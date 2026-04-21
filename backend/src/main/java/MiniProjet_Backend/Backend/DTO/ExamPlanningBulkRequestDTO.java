package MiniProjet_Backend.Backend.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ExamPlanningBulkRequestDTO {
    @NotEmpty
    @Valid
    private List<ExamPlanningRequestDTO> exams;
}
