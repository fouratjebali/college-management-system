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
public class ProfessorDashboardResponseDTO {
    private List<StatDTO> stats;
    private List<TeachingRowDTO> teachings;
    private List<GroupRowDTO> groups;
    private List<SessionRowDTO> sessions;
    private List<EvaluationRowDTO> evaluations;
    private List<StudentRowDTO> students;
    private List<GradeRowDTO> grades;
    private List<AttendanceRowDTO> attendance;
    private List<MaterialRowDTO> materials;

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
    public static class TeachingRowDTO {
        private Integer id;
        private String code;
        private String title;
        private String semester;
        private String year;
        private String department;
        private String coefficient;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupRowDTO {
        private Integer id;
        private String label;
        private String level;
        private String year;
        private String department;
        private Integer studentCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionRowDTO {
        private Integer id;
        private Integer teachingId;
        private String subject;
        private Integer groupId;
        private String group;
        private String day;
        private String start;
        private String end;
        private String room;
        private String type;
        private String collectiveAbsenceStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationRowDTO {
        private Integer id;
        private Integer sessionId;
        private String label;
        private String type;
        private String date;
        private String coefficient;
        private Integer groupId;
        private String group;
        private String subject;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRowDTO {
        private Integer id;
        private String name;
        private String email;
        private String matricule;
        private Integer groupId;
        private String group;
        private String level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeRowDTO {
        private Integer studentId;
        private Integer evaluationId;
        private String value;
        private String status;
        private String remark;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceRowDTO {
        private Integer studentId;
        private Integer sessionId;
        private String status;
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialRowDTO {
        private Integer id;
        private Integer teachingId;
        private String title;
        private String fileName;
        private String fileType;
        private String size;
        private String date;
        private String subject;
    }
}
