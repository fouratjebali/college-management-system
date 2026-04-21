package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.ExamPlanningBulkRequestDTO;
import MiniProjet_Backend.Backend.DTO.ExamPlanningOptionDTO;
import MiniProjet_Backend.Backend.DTO.ExamPlanningRequestDTO;
import MiniProjet_Backend.Backend.DTO.ExamPlanningResponseDTO;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Evaluation;
import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Model.Matiere;
import MiniProjet_Backend.Backend.Model.Professeur;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Repository.AcademicYearRepository;
import MiniProjet_Backend.Backend.Repository.DepartementRepository;
import MiniProjet_Backend.Backend.Repository.EnseignementRepository;
import MiniProjet_Backend.Backend.Repository.EvaluationRepository;
import MiniProjet_Backend.Backend.Repository.GroupeRepository;
import MiniProjet_Backend.Backend.Repository.MatiereRepository;
import MiniProjet_Backend.Backend.Repository.ProfesseurRepository;
import MiniProjet_Backend.Backend.Repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

@Service
public class ExamPlanningService {
    private static final String STATUS_DRAFT = "BROUILLON";
    private static final String STATUS_PUBLISHED = "PUBLIE";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final EvaluationRepository evaluationRepository;
    private final EnseignementRepository enseignementRepository;
    private final MatiereRepository matiereRepository;
    private final GroupeRepository groupeRepository;
    private final ProfesseurRepository professeurRepository;
    private final DepartementRepository departementRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final SeanceService seanceService;
    private final AcademicEvaluationPolicyService academicEvaluationPolicyService;

    public ExamPlanningService(
            EvaluationRepository evaluationRepository,
            EnseignementRepository enseignementRepository,
            MatiereRepository matiereRepository,
            GroupeRepository groupeRepository,
            ProfesseurRepository professeurRepository,
            DepartementRepository departementRepository,
            AcademicYearRepository academicYearRepository,
            SemesterRepository semesterRepository,
            SeanceService seanceService,
            AcademicEvaluationPolicyService academicEvaluationPolicyService
    ) {
        this.evaluationRepository = evaluationRepository;
        this.enseignementRepository = enseignementRepository;
        this.matiereRepository = matiereRepository;
        this.groupeRepository = groupeRepository;
        this.professeurRepository = professeurRepository;
        this.departementRepository = departementRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.seanceService = seanceService;
        this.academicEvaluationPolicyService = academicEvaluationPolicyService;
    }

    @Transactional(readOnly = true)
    public ExamPlanningOptionDTO getOptions() {
        return ExamPlanningOptionDTO.builder()
                .departments(departementRepository.findAll().stream()
                        .sorted(Comparator.comparing(departement -> departement.getNom().toLowerCase()))
                        .map(departement -> option(departement.getId(), departement.getNom(), "Departement"))
                        .toList())
                .groups(groupeRepository.findAll().stream()
                        .sorted(Comparator.comparing(Groupe::getLibelle))
                        .map(group -> option(group.getId(), group.getLibelle(), group.getDepartement().getNom()))
                        .toList())
                .subjects(matiereRepository.findAll().stream()
                        .sorted(Comparator.comparing(Matiere::getLibelle))
                        .map(subject -> option(subject.getId(), subject.getLibelle(), subject.getCode()))
                        .toList())
                .professors(professeurRepository.findAll().stream()
                        .sorted(Comparator.comparing(Professeur::getNomComplet))
                        .map(professor -> option(professor.getId(), professor.getNomComplet(), professor.getGrade()))
                        .toList())
                .activeAcademicYear(academicYearRepository.findByActiveTrue().map(year -> year.getLabel()).orElse(""))
                .activeSemester(semesterRepository.findByActiveTrue().map(semester -> semester.getCode()).orElse(""))
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExamPlanningResponseDTO> getExams() {
        return evaluationRepository.findAll().stream()
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .sorted(Comparator.comparing(Evaluation::getDateEvaluation))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExamPlanningResponseDTO createExam(ExamPlanningRequestDTO request) {
        validateRequest(request);

        Matiere subject = matiereRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        Groupe group = groupeRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));
        Professeur professor = professeurRepository.findById(request.getProfessorId())
                .orElseThrow(() -> new RuntimeException("Professor not found"));

        Enseignement teaching = findOrCreateTeaching(subject, professor);

        Seance seance = new Seance();
        seance.setTypeSeance("EXAMEN");
        seance.setJoursemaine(toFrenchDay(request.getExamDate().getDayOfWeek()));
        seance.setHeureDebut(request.getStartTime());
        seance.setHeureFin(request.getEndTime());
        seance.setBatiment(request.getBuilding().trim());
        seance.setSalle(request.getRoom().trim());
        seance.setGroupe(group);
        seance.setEnseignement(teaching);

        Seance savedSeance = seanceService.saveSeance(seance);

        Evaluation evaluation = new Evaluation();
        evaluation.setLibelle(buildEvaluationLabel(request, subject));
        evaluation.setTypeEvaluation(request.getEvaluationType());
        evaluation.setDateEvaluation(LocalDateTime.of(request.getExamDate(), request.getStartTime()));
        evaluation.setCoefficient(1.0F);
        evaluation.setPlanningStatus(STATUS_DRAFT);
        evaluation.setPublishedAt(null);
        evaluation.setSeance(savedSeance);

        Evaluation savedEvaluation = evaluationRepository.save(
                academicEvaluationPolicyService.applyPolicy(evaluation)
        );

        return toResponse(savedEvaluation);
    }

    @Transactional
    public List<ExamPlanningResponseDTO> createExams(ExamPlanningBulkRequestDTO request) {
        return request.getExams().stream()
                .map(this::createExam)
                .toList();
    }

    @Transactional
    public ExamPlanningResponseDTO publishExam(Integer evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        if (!academicEvaluationPolicyService.isAcademicEvaluation(evaluation)) {
            throw new RuntimeException("Only academic evaluations can be published from exam planning");
        }

        publish(evaluation);
        return toResponse(evaluationRepository.save(evaluation));
    }

    @Transactional
    public List<ExamPlanningResponseDTO> publishWeek(LocalDate weekStart) {
        LocalDate normalizedWeekStart = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = normalizedWeekStart.plusDays(6);
        List<Evaluation> evaluations = evaluationRepository.findAll().stream()
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .filter(evaluation -> {
                    LocalDate examDate = evaluation.getDateEvaluation().toLocalDate();
                    return !examDate.isBefore(normalizedWeekStart) && !examDate.isAfter(weekEnd);
                })
                .sorted(Comparator.comparing(Evaluation::getDateEvaluation))
                .toList();

        if (evaluations.isEmpty()) {
            throw new RuntimeException("No exams found for this week");
        }

        evaluations.forEach(this::publish);
        return evaluationRepository.saveAll(evaluations).stream()
                .map(this::toResponse)
                .toList();
    }

    private Enseignement findOrCreateTeaching(Matiere subject, Professeur professor) {
        return enseignementRepository.findByMatiereId(subject.getId()).stream()
                .filter(teaching -> teaching.getProfesseur().getId().equals(professor.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Enseignement teaching = new Enseignement();
                    teaching.setMatiere(subject);
                    teaching.setProfesseur(professor);
                    teaching.setAnneeUniversitaire(
                            academicYearRepository.findByActiveTrue().map(year -> year.getLabel()).orElse("2025-2026")
                    );
                    teaching.setSemestre(resolveActiveSemesterNumber());
                    return enseignementRepository.save(teaching);
                });
    }

    private Integer resolveActiveSemesterNumber() {
        return semesterRepository.findByActiveTrue()
                .map(semester -> semester.getCode().replaceAll("\\D+", ""))
                .filter(value -> !value.isBlank())
                .map(Integer::parseInt)
                .orElse(1);
    }

    private void validateRequest(ExamPlanningRequestDTO request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }
    }

    private String buildEvaluationLabel(ExamPlanningRequestDTO request, Matiere subject) {
        String type = academicEvaluationPolicyService.normalizeEvaluationType(request.getEvaluationType());
        String details = request.getDetails() == null || request.getDetails().isBlank()
                ? ""
                : " - " + request.getDetails().trim();

        return type + " " + subject.getLibelle() + details;
    }

    private ExamPlanningResponseDTO toResponse(Evaluation evaluation) {
        Seance seance = evaluation.getSeance();
        Enseignement teaching = seance.getEnseignement();
        Matiere subject = teaching.getMatiere();
        Groupe group = seance.getGroupe();
        Professeur professor = teaching.getProfesseur();
        LocalDate examDate = evaluation.getDateEvaluation().toLocalDate();

        return ExamPlanningResponseDTO.builder()
                .evaluationId(evaluation.getId())
                .seanceId(seance.getId())
                .subject(subject.getLibelle())
                .subjectCode(subject.getCode())
                .group(group.getLibelle())
                .professor(professor.getNomComplet())
                .date(evaluation.getDateEvaluation().format(DATE_FORMATTER))
                .isoDate(examDate.toString())
                .day(seance.getJoursemaine())
                .weekStart(examDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString())
                .startTime(seance.getHeureDebut().format(TIME_FORMATTER))
                .endTime(seance.getHeureFin().format(TIME_FORMATTER))
                .room(seance.getBatiment() + " / " + seance.getSalle())
                .type(academicEvaluationPolicyService.normalizeEvaluationType(evaluation.getTypeEvaluation()))
                .scope(group.getDepartement().getNom())
                .status(displayStatus(evaluation.getPlanningStatus()))
                .publishedAt(evaluation.getPublishedAt() == null
                        ? ""
                        : evaluation.getPublishedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .build();
    }

    private void publish(Evaluation evaluation) {
        evaluation.setPlanningStatus(STATUS_PUBLISHED);
        evaluation.setPublishedAt(LocalDateTime.now());
    }

    private String displayStatus(String status) {
        if (STATUS_PUBLISHED.equalsIgnoreCase(status)) {
            return "Publie";
        }
        return "Brouillon";
    }

    private ExamPlanningOptionDTO.OptionDTO option(Integer id, String label, String meta) {
        return ExamPlanningOptionDTO.OptionDTO.builder()
                .id(id)
                .label(label)
                .meta(meta)
                .build();
    }

    private String toFrenchDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Lundi";
            case TUESDAY -> "Mardi";
            case WEDNESDAY -> "Mercredi";
            case THURSDAY -> "Jeudi";
            case FRIDAY -> "Vendredi";
            case SATURDAY -> "Samedi";
            case SUNDAY -> "Dimanche";
        };
    }
}
