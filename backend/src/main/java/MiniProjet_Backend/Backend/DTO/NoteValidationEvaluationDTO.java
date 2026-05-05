package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteValidationEvaluationDTO {
    private Integer evaluationId;
    private String label;
    private String type;
    private String subject;
    private String group;
    private String professor;
    private String date;
    private Integer totalNotes;
    private Integer draftCount;
    private Integer submittedCount;
    private Integer validatedCount;
    private Integer rejectedCount;
    private Integer publishedCount;
    private String status;
}
