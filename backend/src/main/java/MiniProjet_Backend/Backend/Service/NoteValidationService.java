package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.NoteValidationDetailDTO;
import MiniProjet_Backend.Backend.DTO.NoteValidationEvaluationDTO;
import MiniProjet_Backend.Backend.DTO.NoteValidationStudentDTO;
import MiniProjet_Backend.Backend.Model.Evaluation;
import MiniProjet_Backend.Backend.Model.Note;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Repository.EvaluationRepository;
import MiniProjet_Backend.Backend.Repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class NoteValidationService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EvaluationRepository evaluationRepository;
    private final NoteRepository noteRepository;
    private final AcademicEvaluationPolicyService academicEvaluationPolicyService;

    public NoteValidationService(
            EvaluationRepository evaluationRepository,
            NoteRepository noteRepository,
            AcademicEvaluationPolicyService academicEvaluationPolicyService
    ) {
        this.evaluationRepository = evaluationRepository;
        this.noteRepository = noteRepository;
        this.academicEvaluationPolicyService = academicEvaluationPolicyService;
    }

    @Transactional(readOnly = true)
    public List<NoteValidationEvaluationDTO> getEvaluations() {
        return evaluationRepository.findAll().stream()
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .sorted(Comparator.comparing(Evaluation::getDateEvaluation).reversed())
                .map(this::toEvaluationRow)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteValidationDetailDTO getEvaluationDetails(Integer evaluationId) {
        Evaluation evaluation = findAcademicEvaluation(evaluationId);
        List<NoteValidationStudentDTO> notes = noteRepository.findByEvaluationId(evaluationId).stream()
                .sorted(Comparator.comparing(note -> note.getEtudiant().getNomComplet()))
                .map(this::toStudentRow)
                .toList();

        return NoteValidationDetailDTO.builder()
                .evaluation(toEvaluationRow(evaluation))
                .notes(notes)
                .build();
    }

    @Transactional
    public NoteValidationDetailDTO validateEvaluation(Integer evaluationId, String remark) {
        Evaluation evaluation = findAcademicEvaluation(evaluationId);
        List<Note> notes = noteRepository.findByEvaluationId(evaluationId);

        notes.stream()
                .filter(note -> NoteWorkflowService.STATUS_SUBMITTED.equalsIgnoreCase(note.getStatut())
                        || NoteWorkflowService.STATUS_REJECTED.equalsIgnoreCase(note.getStatut()))
                .forEach(note -> {
                    note.setStatut(NoteWorkflowService.STATUS_VALIDATED);
                    note.setValidatedAt(LocalDateTime.now());
                    note.setPublishedAt(null);
                    note.setValidationRemark(cleanRemark(remark, "Validee par l'administration."));
                });

        noteRepository.saveAll(notes);
        return getEvaluationDetails(evaluation.getId());
    }

    @Transactional
    public NoteValidationDetailDTO rejectEvaluation(Integer evaluationId, String remark) {
        Evaluation evaluation = findAcademicEvaluation(evaluationId);
        List<Note> notes = noteRepository.findByEvaluationId(evaluationId);

        notes.stream()
                .filter(note -> !NoteWorkflowService.STATUS_PUBLISHED.equalsIgnoreCase(note.getStatut()))
                .forEach(note -> {
                    note.setStatut(NoteWorkflowService.STATUS_REJECTED);
                    note.setValidatedAt(null);
                    note.setPublishedAt(null);
                    note.setValidationRemark(cleanRemark(remark, "Correction demandee par l'administration."));
                });

        noteRepository.saveAll(notes);
        return getEvaluationDetails(evaluation.getId());
    }

    @Transactional
    public NoteValidationDetailDTO publishEvaluation(Integer evaluationId, String remark) {
        Evaluation evaluation = findAcademicEvaluation(evaluationId);
        List<Note> notes = noteRepository.findByEvaluationId(evaluationId);
        List<Note> validatedNotes = notes.stream()
                .filter(note -> NoteWorkflowService.STATUS_VALIDATED.equalsIgnoreCase(note.getStatut()))
                .toList();

        if (validatedNotes.isEmpty()) {
            throw new RuntimeException("No validated notes available for publication");
        }

        validatedNotes.forEach(note -> {
            note.setStatut(NoteWorkflowService.STATUS_PUBLISHED);
            note.setPublishedAt(LocalDateTime.now());
            note.setValidationRemark(cleanRemark(remark, note.getValidationRemark()));
        });

        noteRepository.saveAll(notes);
        return getEvaluationDetails(evaluation.getId());
    }

    private Evaluation findAcademicEvaluation(Integer evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        if (!academicEvaluationPolicyService.isAcademicEvaluation(evaluation)) {
            throw new RuntimeException("Only academic evaluations can be managed here");
        }

        return evaluation;
    }

    private NoteValidationEvaluationDTO toEvaluationRow(Evaluation evaluation) {
        List<Note> notes = noteRepository.findByEvaluationId(evaluation.getId());
        long draftCount = count(notes, NoteWorkflowService.STATUS_DRAFT);
        long submittedCount = count(notes, NoteWorkflowService.STATUS_SUBMITTED);
        long validatedCount = count(notes, NoteWorkflowService.STATUS_VALIDATED);
        long rejectedCount = count(notes, NoteWorkflowService.STATUS_REJECTED);
        long publishedCount = count(notes, NoteWorkflowService.STATUS_PUBLISHED);
        Seance seance = evaluation.getSeance();

        return NoteValidationEvaluationDTO.builder()
                .evaluationId(evaluation.getId())
                .label(evaluation.getLibelle())
                .type(academicEvaluationPolicyService.normalizeEvaluationType(evaluation.getTypeEvaluation()))
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .group(seance.getGroupe().getLibelle())
                .professor(seance.getEnseignement().getProfesseur().getNomComplet())
                .date(evaluation.getDateEvaluation().format(DATE_FORMATTER))
                .totalNotes(notes.size())
                .draftCount((int) draftCount)
                .submittedCount((int) submittedCount)
                .validatedCount((int) validatedCount)
                .rejectedCount((int) rejectedCount)
                .publishedCount((int) publishedCount)
                .status(resolveStatus(notes))
                .build();
    }

    private NoteValidationStudentDTO toStudentRow(Note note) {
        return NoteValidationStudentDTO.builder()
                .noteId(note.getId())
                .studentId(note.getEtudiant().getId())
                .studentName(note.getEtudiant().getNomComplet())
                .matricule(note.getEtudiant().getMatricule())
                .value(Float.toString(note.getValeur()))
                .status(note.getStatut())
                .remark(note.getRemarque())
                .validationRemark(note.getValidationRemark())
                .build();
    }

    private long count(List<Note> notes, String status) {
        return notes.stream()
                .filter(note -> status.equalsIgnoreCase(note.getStatut()))
                .count();
    }

    private String resolveStatus(List<Note> notes) {
        if (notes.isEmpty()) {
            return "Aucune note";
        }
        if (notes.stream().allMatch(note -> NoteWorkflowService.STATUS_PUBLISHED.equalsIgnoreCase(note.getStatut()))) {
            return "Publiee";
        }
        if (notes.stream().anyMatch(note -> NoteWorkflowService.STATUS_REJECTED.equalsIgnoreCase(note.getStatut()))) {
            return "A corriger";
        }
        if (notes.stream().anyMatch(note -> NoteWorkflowService.STATUS_VALIDATED.equalsIgnoreCase(note.getStatut()))) {
            return "Validee";
        }
        if (notes.stream().anyMatch(note -> NoteWorkflowService.STATUS_SUBMITTED.equalsIgnoreCase(note.getStatut()))) {
            return "En validation";
        }
        return "Brouillon";
    }

    private String cleanRemark(String remark, String fallback) {
        return remark == null || remark.isBlank() ? fallback : remark.trim();
    }
}
