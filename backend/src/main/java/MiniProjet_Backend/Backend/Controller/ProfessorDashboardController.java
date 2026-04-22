package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.ProfessorDashboardResponseDTO;
import MiniProjet_Backend.Backend.Service.ProfessorDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
