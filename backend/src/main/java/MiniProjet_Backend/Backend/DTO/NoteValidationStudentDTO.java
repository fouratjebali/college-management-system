package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteValidationStudentDTO {
    private Integer noteId;
    private Integer studentId;
    private String studentName;
    private String matricule;
    private String value;
    private String status;
    private String remark;
    private String validationRemark;
}
