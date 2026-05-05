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
public class NoteValidationDetailDTO {
    private NoteValidationEvaluationDTO evaluation;
    private List<NoteValidationStudentDTO> notes;
}
