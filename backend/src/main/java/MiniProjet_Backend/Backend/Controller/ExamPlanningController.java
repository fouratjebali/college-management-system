package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.ExamPlanningBulkRequestDTO;
import MiniProjet_Backend.Backend.DTO.ExamPlanningOptionDTO;
import MiniProjet_Backend.Backend.DTO.ExamPlanningPublishWeekRequestDTO;
import MiniProjet_Backend.Backend.DTO.ExamPlanningRequestDTO;
import MiniProjet_Backend.Backend.DTO.ExamPlanningResponseDTO;
import MiniProjet_Backend.Backend.Service.ExamPlanningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/exam-planning")
public class ExamPlanningController {
    private final ExamPlanningService examPlanningService;

    public ExamPlanningController(ExamPlanningService examPlanningService) {
        this.examPlanningService = examPlanningService;
    }

    @GetMapping("/options")
    public ResponseEntity<ExamPlanningOptionDTO> getOptions() {
        return ResponseEntity.ok(examPlanningService.getOptions());
    }

    @GetMapping("/exams")
    public ResponseEntity<List<ExamPlanningResponseDTO>> getExams() {
        return ResponseEntity.ok(examPlanningService.getExams());
    }

    @PostMapping("/exams")
    public ResponseEntity<ExamPlanningResponseDTO> createExam(
            @Valid @RequestBody ExamPlanningRequestDTO request
    ) {
        return ResponseEntity.ok(examPlanningService.createExam(request));
    }

    @PostMapping("/exams/bulk")
    public ResponseEntity<List<ExamPlanningResponseDTO>> createExams(
            @Valid @RequestBody ExamPlanningBulkRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examPlanningService.createExams(request));
    }

    @PatchMapping("/exams/{evaluationId}/publish")
    public ResponseEntity<ExamPlanningResponseDTO> publishExam(@PathVariable Integer evaluationId) {
        return ResponseEntity.ok(examPlanningService.publishExam(evaluationId));
    }

    @PatchMapping("/weeks/publish")
    public ResponseEntity<List<ExamPlanningResponseDTO>> publishWeek(
            @Valid @RequestBody ExamPlanningPublishWeekRequestDTO request
    ) {
        return ResponseEntity.ok(examPlanningService.publishWeek(request.getWeekStart()));
    }
}
