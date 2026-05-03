package MiniProjet_Backend.Backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorEvaluationRequestDTO {
    @NotBlank
    private String libelle;

    @NotBlank
    private String typeEvaluation;

    @NotNull
    private LocalDateTime dateEvaluation;

    @NotNull
    private Integer seanceId;
}
