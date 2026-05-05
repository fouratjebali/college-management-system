package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSupervisionStudentDTO {
    private Integer presenceId;
    private Integer studentId;
    private String studentName;
    private String matricule;
    private String status;
    private String date;
}
