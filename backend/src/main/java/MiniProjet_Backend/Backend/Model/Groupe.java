package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.Set;

@Entity
@Table(name="groupe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Groupe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false)
    private String niveau;

    @Column(nullable = false)
    private String anneeUniversitaire;

    @ManyToOne
    @JoinColumn(name="departement_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Departement departement;

    @OneToMany(mappedBy = "groupe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Etudiant> etudiants;

    @OneToMany(mappedBy = "groupe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Seance> seances;
}

