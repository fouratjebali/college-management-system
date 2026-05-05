package MiniProjet_Backend.Backend.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExamPlanningPublishWeekRequestDTO {
    @NotNull
    private LocalDate weekStart;
}
