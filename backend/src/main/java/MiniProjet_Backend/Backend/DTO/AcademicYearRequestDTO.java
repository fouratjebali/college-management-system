package MiniProjet_Backend.Backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AcademicYearRequestDTO {
    @NotBlank
    private String label;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private boolean active;

    private boolean locked;
}
