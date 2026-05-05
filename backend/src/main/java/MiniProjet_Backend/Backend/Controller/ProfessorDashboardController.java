package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.ProfessorDashboardResponseDTO;
import MiniProjet_Backend.Backend.DTO.ProfessorEvaluationRequestDTO;
import MiniProjet_Backend.Backend.Service.ProfessorDashboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/professor")
public class ProfessorDashboardController {
    private final ProfessorDashboardService professorDashboardService;

    public ProfessorDashboardController(ProfessorDashboardService professorDashboardService) {
        this.professorDashboardService = professorDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ProfessorDashboardResponseDTO> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(professorDashboardService.getDashboard(authentication.getName()));
    }

    @PatchMapping("/attendance/{sessionId}/collective-absence")
    public ResponseEntity<Void> reportCollectiveAbsence(
            @PathVariable Integer sessionId,
            Authentication authentication
    ) {
        professorDashboardService.reportCollectiveAbsence(authentication.getName(), sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/evaluations")
    public ResponseEntity<ProfessorDashboardResponseDTO.EvaluationRowDTO> createEvaluation(
            @Valid @RequestBody ProfessorEvaluationRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(professorDashboardService.createEvaluation(authentication.getName(), request));
    }

    @PutMapping("/evaluations/{evaluationId}")
    public ResponseEntity<ProfessorDashboardResponseDTO.EvaluationRowDTO> updateEvaluation(
            @PathVariable Integer evaluationId,
            @Valid @RequestBody ProfessorEvaluationRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(professorDashboardService.updateEvaluation(authentication.getName(), evaluationId, request));
    }

    @DeleteMapping("/evaluations/{evaluationId}")
    public ResponseEntity<Void> deleteEvaluation(
            @PathVariable Integer evaluationId,
            Authentication authentication
    ) {
        professorDashboardService.deleteEvaluation(authentication.getName(), evaluationId);
        return ResponseEntity.noContent().build();
    }
}
