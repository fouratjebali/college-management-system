package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.AttendanceSupervisionDetailDTO;
import MiniProjet_Backend.Backend.DTO.AttendanceSupervisionSessionDTO;
import MiniProjet_Backend.Backend.Service.AttendanceSupervisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/attendance-supervision")
public class AttendanceSupervisionController {
    private final AttendanceSupervisionService attendanceSupervisionService;

    public AttendanceSupervisionController(AttendanceSupervisionService attendanceSupervisionService) {
        this.attendanceSupervisionService = attendanceSupervisionService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AttendanceSupervisionSessionDTO>> getSessions() {
        return ResponseEntity.ok(attendanceSupervisionService.getSessions());
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<AttendanceSupervisionDetailDTO> getSessionDetails(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(attendanceSupervisionService.getSessionDetails(sessionId));
    }

    @PatchMapping("/sessions/{sessionId}/close")
    public ResponseEntity<AttendanceSupervisionDetailDTO> closeSession(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(attendanceSupervisionService.closeSession(sessionId));
    }

    @PatchMapping("/sessions/{sessionId}/reopen")
    public ResponseEntity<AttendanceSupervisionDetailDTO> reopenSession(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(attendanceSupervisionService.reopenSession(sessionId));
    }
}
