package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.StudentDashboardResponseDTO;
import MiniProjet_Backend.Backend.Model.Annonce;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Evaluation;
import MiniProjet_Backend.Backend.Model.Note;
import MiniProjet_Backend.Backend.Model.Presence;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Model.SupportCours;
import MiniProjet_Backend.Backend.Repository.AnnonceRepository;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import MiniProjet_Backend.Backend.Repository.EvaluationRepository;
import MiniProjet_Backend.Backend.Repository.NoteRepository;
import MiniProjet_Backend.Backend.Repository.PresenceRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import MiniProjet_Backend.Backend.Repository.SupportCoursRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@Service
public class StudentDashboardService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EtudiantRepository etudiantRepository;
    private final SeanceRepository seanceRepository;
    private final EvaluationRepository evaluationRepository;
    private final NoteRepository noteRepository;
    private final PresenceRepository presenceRepository;
    private final SupportCoursRepository supportCoursRepository;
    private final AnnonceRepository annonceRepository;
    private final AcademicEvaluationPolicyService academicEvaluationPolicyService;
    private final NoteWorkflowService noteWorkflowService;
    private final AttendanceEliminationService attendanceEliminationService;

    public StudentDashboardService(
            EtudiantRepository etudiantRepository,
            SeanceRepository seanceRepository,
            EvaluationRepository evaluationRepository,
            NoteRepository noteRepository,
            PresenceRepository presenceRepository,
            SupportCoursRepository supportCoursRepository,
            AnnonceRepository annonceRepository,
            AcademicEvaluationPolicyService academicEvaluationPolicyService,
            NoteWorkflowService noteWorkflowService,
            AttendanceEliminationService attendanceEliminationService
    ) {
        this.etudiantRepository = etudiantRepository;
        this.seanceRepository = seanceRepository;
        this.evaluationRepository = evaluationRepository;
        this.noteRepository = noteRepository;
        this.presenceRepository = presenceRepository;
        this.supportCoursRepository = supportCoursRepository;
        this.annonceRepository = annonceRepository;
        this.academicEvaluationPolicyService = academicEvaluationPolicyService;
        this.noteWorkflowService = noteWorkflowService;
        this.attendanceEliminationService = attendanceEliminationService;
    }

    @Transactional
    public StudentDashboardResponseDTO getDashboard(String email) {
        Etudiant student = etudiantRepository.findByEmail(email)
                .orElseGet(() -> etudiantRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Aucun etudiant disponible")));

        List<Seance> schedule = student.getGroupe() == null
                ? List.of()
                : seanceRepository.findByGroupeId(student.getGroupe().getId()).stream()
                .filter(this::isVisibleForStudentSchedule)
                .sorted(Comparator.comparing(Seance::getJoursemaine).thenComparing(Seance::getHeureDebut))
                .toList();
        List<Note> grades = noteRepository.findByEtudiantId(student.getId()).stream()
                .filter(note -> academicEvaluationPolicyService.isAcademicEvaluation(note.getEvaluation()))
                .filter(noteWorkflowService::isPublished)
                .sorted(Comparator.comparing(note -> note.getEvaluation().getDateEvaluation()))
                .toList();
        List<Presence> attendance = presenceRepository.findByEtudiantId(student.getId()).stream()
                .sorted(Comparator.comparing(Presence::getDateSaisie).reversed())
                .toList();
        List<SupportCours> materials = resolveMaterials(schedule);
        List<Annonce> announcements = annonceRepository
                .findVisibleForRole("STUDENT", LocalDateTime.now(), true, PageRequest.of(0, 5))
                .getContent();
        List<Seance> makeups = schedule.stream()
                .filter(seance -> seance.getTypeSeance().equalsIgnoreCase("Rattrapage"))
                .toList();
        List<SubjectAverage> subjectAverages = resolveSubjectAverages(schedule, grades);

        return StudentDashboardResponseDTO.builder()
                .profile(toProfile(student))
                .stats(buildStats(schedule, materials, announcements, attendance))
                .gradeSummary(buildGradeSummary(subjectAverages))
                .subjectGrades(subjectAverages.stream().map(SubjectAverage::row).toList())
                .grades(grades.stream().map(this::toGradeRow).toList())
                .schedule(schedule.stream().map(this::toScheduleRow).toList())
                .materials(materials.stream().map(this::toMaterialRow).toList())
                .announcements(announcements.stream().map(this::toAnnouncementRow).toList())
                .makeups(makeups.stream().map(this::toMakeupRow).toList())
                .attendance(attendance.stream().map(this::toAttendanceRow).toList())
                .absenceSummaries(attendanceEliminationService.getStudentAbsenceSummaries(student.getId()))
                .build();
    }

    private StudentDashboardResponseDTO.ProfileDTO toProfile(Etudiant student) {
        return StudentDashboardResponseDTO.ProfileDTO.builder()
                .id(student.getId())
                .name(student.getNomComplet())
                .email(student.getEmail())
                .matricule(student.getMatricule())
                .level(student.getNiveau())
                .group(student.getGroupe() == null ? "Non affecte" : student.getGroupe().getLibelle())
                .department(student.getGroupe() == null ? "Non affecte" : student.getGroupe().getDepartement().getNom())
                .year(student.getGroupe() == null ? "Non renseigne" : student.getGroupe().getAnneeUniversitaire())
                .build();
    }

    private List<StudentDashboardResponseDTO.StatDTO> buildStats(
            List<Seance> schedule,
            List<SupportCours> materials,
            List<Annonce> announcements,
            List<Presence> attendance
    ) {
        return List.of(
                stat("Seances", Integer.toString(schedule.size()), "Emploi du temps", "steel"),
                stat("Supports", Integer.toString(materials.size()), "Documents disponibles", "warm"),
                stat("Presence", calculateAttendanceRate(attendance), "Taux actuel", "sand"),
                stat("Annonces", Integer.toString(announcements.size()), "Messages actifs", "steel")
        );
    }

    private StudentDashboardResponseDTO.StatDTO stat(
            String label,
            String value,
            String trend,
            String tone
    ) {
        return StudentDashboardResponseDTO.StatDTO.builder()
                .label(label)
                .value(value)
                .trend(trend)
                .tone(tone)
                .build();
    }

    private String calculateAttendanceRate(List<Presence> attendance) {
        if (attendance.isEmpty()) {
            return "--";
        }

        long presentCount = attendance.stream()
                .filter(presence -> !presence.getStatut().equalsIgnoreCase("Absent"))
                .count();
        long rate = Math.round((presentCount * 100.0) / attendance.size());
        return rate + "%";
    }

    private StudentDashboardResponseDTO.GradeSummaryDTO buildGradeSummary(List<SubjectAverage> subjectAverages) {
        int expectedCount = subjectAverages.stream()
                .mapToInt(SubjectAverage::expectedCount)
                .sum();
        int receivedCount = subjectAverages.stream()
                .mapToInt(SubjectAverage::receivedCount)
                .sum();
        boolean complete = !subjectAverages.isEmpty()
                && subjectAverages.stream().allMatch(SubjectAverage::complete);

        if (!complete) {
            String message = expectedCount == 0
                    ? "Aucun DS ou examen configure pour ce semestre."
                    : receivedCount + "/" + expectedCount + " note(s) DS/Examen/Examen TP publiee(s).";
            return StudentDashboardResponseDTO.GradeSummaryDTO.builder()
                    .average("--")
                    .complete(false)
                    .expectedCount(expectedCount)
                    .receivedCount(receivedCount)
                    .message(message)
                    .build();
        }

        double subjectCoefficientSum = subjectAverages.stream()
                .mapToDouble(SubjectAverage::subjectCoefficient)
                .sum();
        if (subjectCoefficientSum <= 0) {
            return StudentDashboardResponseDTO.GradeSummaryDTO.builder()
                    .average("--")
                    .complete(false)
                    .expectedCount(expectedCount)
                    .receivedCount(receivedCount)
                    .message("Les coefficients des matieres ne sont pas disponibles.")
                    .build();
        }

        double weightedAverage = subjectAverages.stream()
                .mapToDouble(subjectAverage -> subjectAverage.average() * subjectAverage.subjectCoefficient())
                .sum() / subjectCoefficientSum;

        return StudentDashboardResponseDTO.GradeSummaryDTO.builder()
                .average(String.format(Locale.US, "%.2f", weightedAverage))
                .complete(true)
                .expectedCount(expectedCount)
                .receivedCount(receivedCount)
                .message("Toutes les notes DS/Examen/Examen TP du semestre sont publiees.")
                .build();
    }

    private List<SubjectAverage> resolveSubjectAverages(List<Seance> schedule, List<Note> grades) {
        List<Enseignement> teachings = uniqueTeachingsBySubject(schedule);
        Map<Integer, Note> gradesByEvaluation = new LinkedHashMap<>();
        grades.forEach(note -> gradesByEvaluation.putIfAbsent(note.getEvaluation().getId(), note));

        return teachings.stream()
                .map(enseignement -> resolveSubjectAverage(enseignement, schedule, gradesByEvaluation))
                .toList();
    }

    private SubjectAverage resolveSubjectAverage(
            Enseignement enseignement,
            List<Seance> schedule,
            Map<Integer, Note> gradesByEvaluation
    ) {
        Seance referenceSession = schedule.stream()
                .filter(seance -> sameSubject(seance.getEnseignement(), enseignement))
                .findFirst()
                .orElseThrow();
        List<Seance> subjectSessions = schedule.stream()
                .filter(seance -> sameSubject(seance.getEnseignement(), enseignement))
                .toList();
        boolean hasTp = hasTpComponent(subjectSessions);
        List<String> requiredTypes = hasTp
                ? List.of(
                AcademicEvaluationPolicyService.TYPE_DS,
                AcademicEvaluationPolicyService.TYPE_EXAMEN_TP,
                AcademicEvaluationPolicyService.TYPE_EXAMEN
        )
                : List.of(AcademicEvaluationPolicyService.TYPE_DS, AcademicEvaluationPolicyService.TYPE_EXAMEN);
        List<Evaluation> evaluations = schedule.stream()
                .filter(seance -> sameSubject(seance.getEnseignement(), enseignement))
                .flatMap(seance -> evaluationRepository.findBySeanceId(seance.getId()).stream())
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .toList();
        Map<String, Evaluation> evaluationsByType = new LinkedHashMap<>();
        evaluations.forEach(evaluation -> evaluationsByType.putIfAbsent(
                academicEvaluationPolicyService.normalizeEvaluationType(evaluation.getTypeEvaluation()),
                evaluation
        ));
        List<StudentDashboardResponseDTO.EvaluationGradeDTO> evaluationGrades = requiredTypes.stream()
                .map(type -> toEvaluationGrade(type, hasTp, evaluationsByType.get(type), gradesByEvaluation))
                .toList();
        List<Note> subjectNotes = requiredTypes.stream()
                .map(evaluationsByType::get)
                .filter(evaluation -> evaluation != null)
                .map(evaluation -> gradesByEvaluation.get(evaluation.getId()))
                .filter(note -> note != null)
                .toList();
        int expectedCount = requiredTypes.size();
        int receivedCount = subjectNotes.size();
        boolean complete = expectedCount > 0 && expectedCount == receivedCount;
        double average = complete ? calculateSubjectAverage(subjectNotes) : 0;
        double subjectCoefficient = enseignement.getMatiere().getCoefficient() == null
                ? 0
                : enseignement.getMatiere().getCoefficient();
        StudentDashboardResponseDTO.SubjectGradeDTO row = StudentDashboardResponseDTO.SubjectGradeDTO.builder()
                .subject(enseignement.getMatiere().getLibelle())
                .professor(referenceSession.getEnseignement().getProfesseur().getNomComplet())
                .subjectCoefficient(formatNumber(subjectCoefficient))
                .average(complete ? String.format(Locale.US, "%.2f", average) : "--")
                .complete(complete)
                .expectedCount(expectedCount)
                .receivedCount(receivedCount)
                .evaluations(evaluationGrades)
                .build();

        return new SubjectAverage(average, subjectCoefficient, expectedCount, receivedCount, complete, row);
    }

    private StudentDashboardResponseDTO.EvaluationGradeDTO toEvaluationGrade(
            String type,
            boolean hasTp,
            Evaluation evaluation,
            Map<Integer, Note> gradesByEvaluation
    ) {
        Note note = evaluation == null ? null : gradesByEvaluation.get(evaluation.getId());

        return StudentDashboardResponseDTO.EvaluationGradeDTO.builder()
                .type(type)
                .label(evaluation == null ? type + " non planifie" : evaluation.getLibelle())
                .value(note == null ? "--" : Float.toString(note.getValeur()))
                .coefficient(formatNumber(academicEvaluationPolicyService.effectiveCoefficient(type, hasTp)))
                .date(evaluation == null ? "--" : evaluation.getDateEvaluation().format(DATE_TIME_FORMATTER))
                .status(note == null ? "En attente" : note.getStatut())
                .remark(note == null ? "" : note.getRemarque())
                .published(note != null)
                .build();
    }

    private boolean hasTpComponent(List<Seance> subjectSessions) {
        return subjectSessions.stream().anyMatch(seance -> "TP".equalsIgnoreCase(seance.getTypeSeance()))
                || subjectSessions.stream()
                .flatMap(seance -> evaluationRepository.findBySeanceId(seance.getId()).stream())
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .anyMatch(evaluation -> AcademicEvaluationPolicyService.TYPE_EXAMEN_TP.equals(
                        academicEvaluationPolicyService.normalizeEvaluationType(evaluation.getTypeEvaluation())
                ));
    }

    private List<Enseignement> uniqueTeachingsBySubject(List<Seance> schedule) {
        Map<Integer, Enseignement> teachings = new LinkedHashMap<>();
        schedule.forEach(seance -> teachings.putIfAbsent(
                seance.getEnseignement().getMatiere().getId(),
                seance.getEnseignement()
        ));
        return teachings.values().stream().toList();
    }

    private boolean sameSubject(Enseignement first, Enseignement second) {
        return first.getMatiere().getId().equals(second.getMatiere().getId());
    }

    private double calculateSubjectAverage(List<Note> grades) {
        return grades.stream()
                .mapToDouble(note -> note.getValeur()
                        * academicEvaluationPolicyService.effectiveCoefficient(note.getEvaluation()))
                .sum() / 10.0;
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Integer.toString((int) value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private List<SupportCours> resolveMaterials(List<Seance> schedule) {
        Map<Integer, Enseignement> teachings = new LinkedHashMap<>();
        schedule.forEach(seance -> teachings.putIfAbsent(
                seance.getEnseignement().getId(),
                seance.getEnseignement()
        ));

        return teachings.values().stream()
                .flatMap(enseignement -> supportCoursRepository.findByEnseignementId(enseignement.getId()).stream())
                .sorted(Comparator.comparing(SupportCours::getDateDepot).reversed())
                .toList();
    }

    private StudentDashboardResponseDTO.GradeRowDTO toGradeRow(Note note) {
        Seance seance = note.getEvaluation().getSeance();

        return StudentDashboardResponseDTO.GradeRowDTO.builder()
                .id(note.getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .evaluation(note.getEvaluation().getLibelle())
                .type(academicEvaluationPolicyService.normalizeEvaluationType(note.getEvaluation().getTypeEvaluation()))
                .date(note.getEvaluation().getDateEvaluation().format(DATE_TIME_FORMATTER))
                .value(Float.toString(note.getValeur()))
                .coefficient(Float.toString(academicEvaluationPolicyService.effectiveCoefficient(note.getEvaluation())))
                .status(note.getStatut())
                .remark(note.getRemarque())
                .build();
    }

    private StudentDashboardResponseDTO.ScheduleRowDTO toScheduleRow(Seance seance) {
        return StudentDashboardResponseDTO.ScheduleRowDTO.builder()
                .id(seance.getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .professor(seance.getEnseignement().getProfesseur().getNomComplet())
                .day(seance.getJoursemaine())
                .start(seance.getHeureDebut().toString())
                .end(seance.getHeureFin().toString())
                .room(seance.getBatiment() + " / " + seance.getSalle())
                .type(seance.getTypeSeance())
                .build();
    }

    private boolean isVisibleForStudentSchedule(Seance seance) {
        if (!"EXAMEN".equalsIgnoreCase(seance.getTypeSeance())) {
            return true;
        }

        return evaluationRepository.findBySeanceId(seance.getId()).stream()
                .anyMatch(evaluation -> "PUBLIE".equalsIgnoreCase(evaluation.getPlanningStatus()));
    }

    private StudentDashboardResponseDTO.MaterialRowDTO toMaterialRow(SupportCours supportCours) {
        return StudentDashboardResponseDTO.MaterialRowDTO.builder()
                .id(supportCours.getId())
                .title(supportCours.getTitre())
                .subject(supportCours.getEnseignement().getMatiere().getLibelle())
                .fileName(supportCours.getNomFichierOriginal())
                .fileType(supportCours.getTypeFichier())
                .size(formatBytes(supportCours.getTailleOctets()))
                .date(supportCours.getDateDepot().format(DATE_TIME_FORMATTER))
                .downloadUrl("/api/supports/" + supportCours.getId() + "/download")
                .build();
    }

    private StudentDashboardResponseDTO.AnnouncementRowDTO toAnnouncementRow(Annonce annonce) {
        return StudentDashboardResponseDTO.AnnouncementRowDTO.builder()
                .id(annonce.getId())
                .title(annonce.getTitre())
                .content(annonce.getContenu())
                .publicationDate(annonce.getDatePublication().format(DATE_TIME_FORMATTER))
                .expirationDate(annonce.getDateExpiration().format(DATE_TIME_FORMATTER))
                .author(annonce.getAdministrateur() == null ? "Administration" : annonce.getAdministrateur().getNomComplet())
                .build();
    }

    private StudentDashboardResponseDTO.MakeupRowDTO toMakeupRow(Seance seance) {
        return StudentDashboardResponseDTO.MakeupRowDTO.builder()
                .id(seance.getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .professor(seance.getEnseignement().getProfesseur().getNomComplet())
                .day(seance.getJoursemaine())
                .start(seance.getHeureDebut().toString())
                .end(seance.getHeureFin().toString())
                .room(seance.getBatiment() + " / " + seance.getSalle())
                .build();
    }

    private StudentDashboardResponseDTO.AttendanceRowDTO toAttendanceRow(Presence presence) {
        Seance seance = presence.getSeance();

        return StudentDashboardResponseDTO.AttendanceRowDTO.builder()
                .id(presence.getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .session(seance.getJoursemaine() + " " + seance.getHeureDebut() + " - " + seance.getHeureFin())
                .status(presence.getStatut())
                .date(presence.getDateSaisie().format(DATE_TIME_FORMATTER))
                .build();
    }

    private String formatBytes(Long size) {
        if (size == null || size <= 0) {
            return "0 Ko";
        }

        if (size < 1024 * 1024) {
            return Math.max(1, size / 1024) + " Ko";
        }

        return String.format("%.1f Mo", size / (1024.0 * 1024.0));
    }

    private record SubjectAverage(
            double average,
            double subjectCoefficient,
            int expectedCount,
            int receivedCount,
            boolean complete,
            StudentDashboardResponseDTO.SubjectGradeDTO row
    ) {
    }
}
