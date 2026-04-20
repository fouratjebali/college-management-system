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
public class StudentDashboardResponseDTO {
    private ProfileDTO profile;
    private List<StatDTO> stats;
    private List<GradeRowDTO> grades;
    private List<ScheduleRowDTO> schedule;
    private List<MaterialRowDTO> materials;
    private List<AnnouncementRowDTO> announcements;
    private List<MakeupRowDTO> makeups;
    private List<AttendanceRowDTO> attendance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileDTO {
        private Integer id;
        private String name;
        private String email;
        private String matricule;
        private String level;
        private String group;
        private String department;
        private String year;
    }

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
    public static class GradeRowDTO {
        private Integer id;
        private String subject;
        private String evaluation;
        private String type;
        private String date;
        private String value;
        private String coefficient;
        private String status;
        private String remark;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleRowDTO {
        private Integer id;
        private String subject;
        private String professor;
        private String day;
        private String start;
        private String end;
        private String room;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialRowDTO {
        private Integer id;
        private String title;
        private String subject;
        private String fileName;
        private String fileType;
        private String size;
        private String date;
        private String downloadUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnnouncementRowDTO {
        private Integer id;
        private String title;
        private String content;
        private String publicationDate;
        private String expirationDate;
        private String author;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MakeupRowDTO {
        private Integer id;
        private String subject;
        private String professor;
        private String day;
        private String start;
        private String end;
        private String room;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceRowDTO {
        private Integer id;
        private String subject;
        private String session;
        private String status;
        private String date;
    }
}
