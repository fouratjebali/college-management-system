package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantDTO {
    private Integer id;
    private String nomComplet;
    private String email;
    private boolean actif;
    private String matricule;
    private String niveau;
    private Integer groupeId;
}

