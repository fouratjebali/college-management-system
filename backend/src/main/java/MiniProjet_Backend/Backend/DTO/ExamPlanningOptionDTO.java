package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamPlanningOptionDTO {
    private List<OptionDTO> departments;
    private List<OptionDTO> groups;
    private List<OptionDTO> subjects;
    private List<OptionDTO> professors;
    private String activeAcademicYear;
    private String activeSemester;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        private Integer id;
        private String label;
        private String meta;
    }
}
