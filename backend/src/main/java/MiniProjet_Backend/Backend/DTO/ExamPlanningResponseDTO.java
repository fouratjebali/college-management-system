package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamPlanningResponseDTO {
    private Integer evaluationId;
    private Integer seanceId;
    private String subject;
    private String subjectCode;
    private String group;
    private String professor;
    private String date;
    private String isoDate;
    private String day;
    private String weekStart;
    private String startTime;
    private String endTime;
    private String room;
    private String type;
    private String scope;
    private String status;
    private String publishedAt;
}
