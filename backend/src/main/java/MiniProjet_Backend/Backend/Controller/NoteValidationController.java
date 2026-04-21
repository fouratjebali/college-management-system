package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.NoteValidationDecisionRequestDTO;
import MiniProjet_Backend.Backend.DTO.NoteValidationDetailDTO;
import MiniProjet_Backend.Backend.DTO.NoteValidationEvaluationDTO;
import MiniProjet_Backend.Backend.Service.NoteValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/note-validation")
public class NoteValidationController {
    private final NoteValidationService noteValidationService;

    public NoteValidationController(NoteValidationService noteValidationService) {
        this.noteValidationService = noteValidationService;
    }

    @GetMapping("/evaluations")
    public ResponseEntity<List<NoteValidationEvaluationDTO>> getEvaluations() {
        return ResponseEntity.ok(noteValidationService.getEvaluations());
    }

    @GetMapping("/evaluations/{evaluationId}")
    public ResponseEntity<NoteValidationDetailDTO> getEvaluationDetails(@PathVariable Integer evaluationId) {
        return ResponseEntity.ok(noteValidationService.getEvaluationDetails(evaluationId));
    }

    @PatchMapping("/evaluations/{evaluationId}/validate")
    public ResponseEntity<NoteValidationDetailDTO> validateEvaluation(
            @PathVariable Integer evaluationId,
            @RequestBody(required = false) NoteValidationDecisionRequestDTO request
    ) {
        return ResponseEntity.ok(noteValidationService.validateEvaluation(
                evaluationId,
                request == null ? null : request.getRemark()
        ));
    }

    @PatchMapping("/evaluations/{evaluationId}/reject")
    public ResponseEntity<NoteValidationDetailDTO> rejectEvaluation(
            @PathVariable Integer evaluationId,
            @RequestBody(required = false) NoteValidationDecisionRequestDTO request
    ) {
        return ResponseEntity.ok(noteValidationService.rejectEvaluation(
                evaluationId,
                request == null ? null : request.getRemark()
        ));
    }

    @PatchMapping("/evaluations/{evaluationId}/publish")
    public ResponseEntity<NoteValidationDetailDTO> publishEvaluation(
            @PathVariable Integer evaluationId,
            @RequestBody(required = false) NoteValidationDecisionRequestDTO request
    ) {
        return ResponseEntity.ok(noteValidationService.publishEvaluation(
                evaluationId,
                request == null ? null : request.getRemark()
        ));
    }
}
