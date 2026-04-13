package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name="seance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String typeSeance;

    @Column(nullable = false)
    private String joursemaine;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;

    @Column(nullable = false)
    private String salle;

    @Column(nullable = false)
    private String batiment;

    @ManyToOne
    @JoinColumn(name="enseignement_id", nullable = false)
    private Enseignement enseignement;

    @ManyToOne
    @JoinColumn(name="groupe_id", nullable = false)
    private Groupe groupe;

    @OneToMany(mappedBy = "seance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Presence> presences;

    @OneToMany(mappedBy = "seance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Evaluation> evaluations;
}

