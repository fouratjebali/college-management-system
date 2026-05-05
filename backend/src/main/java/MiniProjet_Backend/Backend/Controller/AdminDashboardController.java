package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.AdminDashboardResponseDTO;
import MiniProjet_Backend.Backend.DTO.AdminUserPageResponseDTO;
import MiniProjet_Backend.Backend.Service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/users")
    public ResponseEntity<AdminUserPageResponseDTO> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "Tous") String role,
            @RequestParam(defaultValue = "Tous") String department,
            @RequestParam(defaultValue = "Tous") String group
    ) {
        return ResponseEntity.ok(adminDashboardService.getUsersPage(page, size, search, role, department, group));
    }
}
