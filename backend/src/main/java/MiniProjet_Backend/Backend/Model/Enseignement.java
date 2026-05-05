package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.Set;

@Entity
@Table(name="enseignement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enseignement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer semestre;

    @Column(nullable = false)
    private String anneeUniversitaire;

    @ManyToOne
    @JoinColumn(name="professeur_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Professeur professeur;

    @ManyToOne
    @JoinColumn(name="matiere_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Matiere matiere;

    @OneToMany(mappedBy = "enseignement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Seance> seances;

    @OneToMany(mappedBy = "enseignement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<SupportCours> supports;
}

