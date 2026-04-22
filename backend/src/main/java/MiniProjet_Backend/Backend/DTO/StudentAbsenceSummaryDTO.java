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
public class StudentAbsenceSummaryDTO {
    private String subject;
    private String professor;
    private String typeSeance;
    private Integer absenceCount;
    private Integer threshold;
    private String status;
    private String notifiedAt;
    private List<StudentAbsenceEntryDTO> absences;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAbsenceEntryDTO {
        private Integer presenceId;
        private String session;
        private String date;
        private String room;
    }
}
