package MiniProjet_Backend.Backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Student Management System Backend");
        response.put("version", "1.0.0");
        response.put("status", "Running");
        response.put("endpoints", Map.of(
                "health", "GET /api/health",
                "api_docs", "GET /api_documentation.md",
                "auth", "POST /api/auth/login",
                "students", "GET /api/etudiants"
        ));
        response.put("documentation", "See README.md or QUICK_START.md for detailed instructions");
        return ResponseEntity.ok(response);
    }
}

