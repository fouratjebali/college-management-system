package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.ProfessorDashboardResponseDTO;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Evaluation;
import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Model.Note;
import MiniProjet_Backend.Backend.Model.Presence;
import MiniProjet_Backend.Backend.Model.Professeur;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Model.SupportCours;
import MiniProjet_Backend.Backend.Repository.EnseignementRepository;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import MiniProjet_Backend.Backend.Repository.EvaluationRepository;
import MiniProjet_Backend.Backend.Repository.NoteRepository;
import MiniProjet_Backend.Backend.Repository.PresenceRepository;
import MiniProjet_Backend.Backend.Repository.ProfesseurRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import MiniProjet_Backend.Backend.Repository.SupportCoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfessorDashboardService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProfesseurRepository professeurRepository;
    private final EnseignementRepository enseignementRepository;
    private final SeanceRepository seanceRepository;
    private final EvaluationRepository evaluationRepository;
    private final EtudiantRepository etudiantRepository;
    private final NoteRepository noteRepository;
    private final PresenceRepository presenceRepository;
    private final SupportCoursRepository supportCoursRepository;
    private final AcademicEvaluationPolicyService academicEvaluationPolicyService;

    public ProfessorDashboardService(
            ProfesseurRepository professeurRepository,
            EnseignementRepository enseignementRepository,
            SeanceRepository seanceRepository,
            EvaluationRepository evaluationRepository,
            EtudiantRepository etudiantRepository,
            NoteRepository noteRepository,
            PresenceRepository presenceRepository,
            SupportCoursRepository supportCoursRepository,
            AcademicEvaluationPolicyService academicEvaluationPolicyService
    ) {
        this.professeurRepository = professeurRepository;
        this.enseignementRepository = enseignementRepository;
        this.seanceRepository = seanceRepository;
        this.evaluationRepository = evaluationRepository;
        this.etudiantRepository = etudiantRepository;
        this.noteRepository = noteRepository;
        this.presenceRepository = presenceRepository;
        this.supportCoursRepository = supportCoursRepository;
        this.academicEvaluationPolicyService = academicEvaluationPolicyService;
    }

    @Transactional(readOnly = true)
    public ProfessorDashboardResponseDTO getDashboard(String email) {
        Professeur professeur = professeurRepository.findByEmail(email)
                .orElseGet(() -> professeurRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Aucun professeur disponible")));

        List<Enseignement> teachings = enseignementRepository.findByProfesseurId(professeur.getId());
        List<Seance> sessions = teachings.stream()
                .flatMap(enseignement -> seanceRepository.findByEnseignementId(enseignement.getId()).stream())
                .sorted(Comparator.comparing(Seance::getJoursemaine).thenComparing(Seance::getHeureDebut))
                .toList();
        List<Evaluation> evaluations = sessions.stream()
                .flatMap(seance -> evaluationRepository.findBySeanceId(seance.getId()).stream())
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .sorted(Comparator.comparing(Evaluation::getDateEvaluation))
                .toList();
        List<Groupe> groups = uniqueGroups(sessions);
        List<Etudiant> students = groups.stream()
                .flatMap(group -> etudiantRepository.findByGroupeId(group.getId()).stream())
                .sorted(Comparator.comparing(Etudiant::getNomComplet))
                .toList();
        List<SupportCours> materials = teachings.stream()
                .flatMap(enseignement -> supportCoursRepository.findByEnseignementId(enseignement.getId()).stream())
                .sorted(Comparator.comparing(SupportCours::getDateDepot).reversed())
                .toList();

        return ProfessorDashboardResponseDTO.builder()
                .stats(buildStats(teachings, groups, sessions, materials))
                .teachings(teachings.stream().map(this::toTeachingRow).toList())
                .groups(groups.stream().map(this::toGroupRow).toList())
                .sessions(sessions.stream().map(this::toSessionRow).toList())
                .evaluations(evaluations.stream().map(this::toEvaluationRow).toList())
                .students(students.stream().map(this::toStudentRow).toList())
                .grades(evaluations.stream()
                        .flatMap(evaluation -> noteRepository.findByEvaluationId(evaluation.getId()).stream())
                        .map(this::toGradeRow)
                        .toList())
                .attendance(sessions.stream()
                        .flatMap(seance -> presenceRepository.findBySeanceId(seance.getId()).stream())
                        .map(this::toAttendanceRow)
                        .toList())
                .materials(materials.stream().map(this::toMaterialRow).toList())
                .build();
    }

    @Transactional
    public void reportCollectiveAbsence(String email, Integer sessionId) {
        Professeur professeur = professeurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Professeur introuvable"));
        Seance seance = seanceRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Seance not found"));

        if (!seance.getEnseignement().getProfesseur().getId().equals(professeur.getId())) {
            throw new RuntimeException("Cette seance n'appartient pas au professeur connecte.");
        }
        if ("CLOTUREE".equalsIgnoreCase(seance.getAttendanceStatus())) {
            throw new RuntimeException("Cette seance de presence est deja cloturee.");
        }

        seance.setCollectiveAbsenceStatus("SIGNALEE");
        seance.setCollectiveAbsenceReportedAt(java.time.LocalDateTime.now());
        seance.setCollectiveAbsenceConfirmedAt(null);
        seanceRepository.save(seance);
    }

    private List<ProfessorDashboardResponseDTO.StatDTO> buildStats(
            List<Enseignement> teachings,
            List<Groupe> groups,
            List<Seance> sessions,
            List<SupportCours> materials
    ) {
        return List.of(
                stat("Enseignements", teachings.size(), "Modules assignes", "light"),
                stat("Groupes", groups.size(), "Groupes suivis", "steel"),
                stat("Seances", sessions.size(), "Cours planifies", "warm"),
                stat("Supports", materials.size(), "Fichiers partages", "sand")
        );
    }

    private ProfessorDashboardResponseDTO.StatDTO stat(
            String label,
            long value,
            String trend,
            String tone
    ) {
        return ProfessorDashboardResponseDTO.StatDTO.builder()
                .label(label)
                .value(Long.toString(value))
                .trend(trend)
                .tone(tone)
                .build();
    }

    private List<Groupe> uniqueGroups(List<Seance> sessions) {
        Map<Integer, Groupe> groups = new LinkedHashMap<>();

        sessions.forEach(seance -> {
            if (seance.getGroupe() != null) {
                groups.putIfAbsent(seance.getGroupe().getId(), seance.getGroupe());
            }
        });

        return groups.values().stream()
                .sorted(Comparator.comparing(Groupe::getLibelle))
                .toList();
    }

    private ProfessorDashboardResponseDTO.TeachingRowDTO toTeachingRow(Enseignement enseignement) {
        return ProfessorDashboardResponseDTO.TeachingRowDTO.builder()
                .id(enseignement.getId())
                .code(enseignement.getMatiere().getCode())
                .title(enseignement.getMatiere().getLibelle())
                .semester("S" + enseignement.getSemestre())
                .year(enseignement.getAnneeUniversitaire())
                .department(enseignement.getMatiere().getDepartement().getNom())
                .coefficient(Float.toString(enseignement.getMatiere().getCoefficient()))
                .build();
    }

    private ProfessorDashboardResponseDTO.GroupRowDTO toGroupRow(Groupe groupe) {
        return ProfessorDashboardResponseDTO.GroupRowDTO.builder()
                .id(groupe.getId())
                .label(groupe.getLibelle())
                .level(groupe.getNiveau())
                .year(groupe.getAnneeUniversitaire())
                .department(groupe.getDepartement().getNom())
                .studentCount(etudiantRepository.findByGroupeId(groupe.getId()).size())
                .build();
    }

    private ProfessorDashboardResponseDTO.SessionRowDTO toSessionRow(Seance seance) {
        return ProfessorDashboardResponseDTO.SessionRowDTO.builder()
                .id(seance.getId())
                .teachingId(seance.getEnseignement().getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .groupId(seance.getGroupe().getId())
                .group(seance.getGroupe().getLibelle())
                .day(seance.getJoursemaine())
                .start(seance.getHeureDebut().toString())
                .end(seance.getHeureFin().toString())
                .room(seance.getBatiment() + " / " + seance.getSalle())
                .type(seance.getTypeSeance())
                .collectiveAbsenceStatus(displayCollectiveAbsenceStatus(seance.getCollectiveAbsenceStatus()))
                .build();
    }

    private String displayCollectiveAbsenceStatus(String status) {
        if ("SIGNALEE".equalsIgnoreCase(status)) {
            return "Signalee";
        }
        if ("VALIDEE".equalsIgnoreCase(status)) {
            return "Validee";
        }
        return "Aucune";
    }

    private ProfessorDashboardResponseDTO.EvaluationRowDTO toEvaluationRow(Evaluation evaluation) {
        Seance seance = evaluation.getSeance();

        return ProfessorDashboardResponseDTO.EvaluationRowDTO.builder()
                .id(evaluation.getId())
                .sessionId(seance.getId())
                .label(evaluation.getLibelle())
                .type(academicEvaluationPolicyService.normalizeEvaluationType(evaluation.getTypeEvaluation()))
                .date(evaluation.getDateEvaluation().format(DATE_TIME_FORMATTER))
                .coefficient(Float.toString(academicEvaluationPolicyService.effectiveCoefficient(evaluation)))
                .groupId(seance.getGroupe().getId())
                .group(seance.getGroupe().getLibelle())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .build();
    }

    private ProfessorDashboardResponseDTO.StudentRowDTO toStudentRow(Etudiant etudiant) {
        return ProfessorDashboardResponseDTO.StudentRowDTO.builder()
                .id(etudiant.getId())
                .name(etudiant.getNomComplet())
                .email(etudiant.getEmail())
                .matricule(etudiant.getMatricule())
                .groupId(etudiant.getGroupe().getId())
                .group(etudiant.getGroupe().getLibelle())
                .level(etudiant.getNiveau())
                .build();
    }

    private ProfessorDashboardResponseDTO.GradeRowDTO toGradeRow(Note note) {
        return ProfessorDashboardResponseDTO.GradeRowDTO.builder()
                .studentId(note.getEtudiant().getId())
                .evaluationId(note.getEvaluation().getId())
                .value(Float.toString(note.getValeur()))
                .status(note.getStatut())
                .remark(note.getRemarque())
                .build();
    }

    private ProfessorDashboardResponseDTO.AttendanceRowDTO toAttendanceRow(Presence presence) {
        return ProfessorDashboardResponseDTO.AttendanceRowDTO.builder()
                .studentId(presence.getEtudiant().getId())
                .sessionId(presence.getSeance().getId())
                .status(presence.getStatut())
                .date(presence.getDateSaisie().format(DATE_TIME_FORMATTER))
                .build();
    }

    private ProfessorDashboardResponseDTO.MaterialRowDTO toMaterialRow(SupportCours supportCours) {
        return ProfessorDashboardResponseDTO.MaterialRowDTO.builder()
                .id(supportCours.getId())
                .teachingId(supportCours.getEnseignement().getId())
                .title(supportCours.getTitre())
                .fileName(supportCours.getNomFichierOriginal())
                .fileType(supportCours.getTypeFichier())
                .size(formatBytes(supportCours.getTailleOctets()))
                .date(supportCours.getDateDepot().format(DATE_TIME_FORMATTER))
                .subject(supportCours.getEnseignement().getMatiere().getLibelle())
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
}
