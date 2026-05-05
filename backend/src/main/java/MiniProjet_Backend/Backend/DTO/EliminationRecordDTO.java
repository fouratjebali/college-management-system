package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EliminationRecordDTO {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private String matricule;
    private String group;
    private String subject;
    private String typeSeance;
    private Integer absenceCount;
    private String status;
    private String detectedAt;
    private String notifiedAt;
}
