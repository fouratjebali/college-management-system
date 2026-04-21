package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearResponseDTO {
    private Integer id;
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private boolean locked;
    private String status;
    private List<SemesterResponseDTO> semesters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemesterResponseDTO {
        private Integer id;
        private Integer academicYearId;
        private String code;
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean active;
        private boolean locked;
        private String status;
    }
}
