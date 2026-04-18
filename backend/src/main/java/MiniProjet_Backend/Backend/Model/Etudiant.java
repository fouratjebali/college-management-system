package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="etudiant")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Etudiant extends User {
    @Column(nullable = false, unique = true)
    private String matricule;

    @Column(nullable = false)
    private String niveau;

    @ManyToOne
    @JoinColumn(name="groupe_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Groupe groupe;
}

