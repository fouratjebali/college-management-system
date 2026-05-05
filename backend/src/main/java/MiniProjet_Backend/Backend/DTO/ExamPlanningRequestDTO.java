package MiniProjet_Backend.Backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ExamPlanningRequestDTO {
    @NotBlank
    private String evaluationType;

    @NotNull
    private Integer subjectId;

    @NotNull
    private Integer groupId;

    @NotNull
    private Integer professorId;

    @NotNull
    private LocalDate examDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotBlank
    private String building;

    @NotBlank
    private String room;

    private String details;
}
