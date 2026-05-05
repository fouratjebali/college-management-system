package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesseurDTO {
    private Integer id;
    private String nomComplet;
    private String email;
    private boolean actif;
    private String matriculePro;
    private String grade;
}

