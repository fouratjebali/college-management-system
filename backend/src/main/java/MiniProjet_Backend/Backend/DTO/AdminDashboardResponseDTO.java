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
public class AdminDashboardResponseDTO {
    private List<StatDTO> stats;
    private List<AcademicRowDTO> academicRows;
    private List<UserRowDTO> users;
    private List<ExamRowDTO> exams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatDTO {
        private String label;
        private String value;
        private String trend;
        private String tone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicRowDTO {
        private String code;
        private String title;
        private String meta;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRowDTO {
        private String name;
        private String email;
        private String role;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamRowDTO {
        private String subject;
        private String group;
        private String date;
        private String room;
    }
}
