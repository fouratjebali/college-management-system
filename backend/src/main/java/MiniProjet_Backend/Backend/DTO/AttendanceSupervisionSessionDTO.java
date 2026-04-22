package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSupervisionSessionDTO {
    private Integer sessionId;
    private String subject;
    private Integer groupId;
    private String group;
    private String department;
    private String professor;
    private String day;
    private String startTime;
    private String endTime;
    private String room;
    private String type;
    private Integer expectedCount;
    private Integer recordedCount;
    private Integer presentCount;
    private Integer lateCount;
    private Integer absentCount;
    private Integer missingCount;
    private String absenceRate;
    private String status;
    private String closedAt;
    private String lastEntryAt;
}
