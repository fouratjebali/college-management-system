package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "elimination_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"etudiant_id", "matiere_id", "type_seance"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EliminationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String typeSeance;

    @Column(nullable = false)
    private Integer absenceCount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    @Column
    private LocalDateTime notifiedAt;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "matiere_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Matiere matiere;
}
