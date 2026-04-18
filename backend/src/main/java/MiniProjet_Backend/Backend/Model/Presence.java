package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name="presence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String statut;

    @Column(nullable = false)
    private LocalDateTime dateSaisie;

    @ManyToOne
    @JoinColumn(name="etudiant_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name="seance_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Seance seance;
}

