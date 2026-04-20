package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.StudentDashboardResponseDTO;
import MiniProjet_Backend.Backend.Service.StudentDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentDashboardController {
    private final StudentDashboardService studentDashboardService;

    public StudentDashboardController(StudentDashboardService studentDashboardService) {
        this.studentDashboardService = studentDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponseDTO> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(studentDashboardService.getDashboard(authentication.getName()));
    }
}
