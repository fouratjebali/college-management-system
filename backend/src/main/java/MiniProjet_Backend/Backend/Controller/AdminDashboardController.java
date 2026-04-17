package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.AdminDashboardResponseDTO;
import MiniProjet_Backend.Backend.Service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponseDTO> getDashboard() {
        return ResponseEntity.ok(adminDashboardService.getDashboard());
    }
}
