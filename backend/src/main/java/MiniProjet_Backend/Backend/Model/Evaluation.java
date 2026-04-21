package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="evaluation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false)
    private String typeEvaluation;

    @Column(nullable = false)
    private LocalDateTime dateEvaluation;

    @Column(nullable = false)
    private Float coefficient;

    @Column
    private String planningStatus = "BROUILLON";

    @Column
    private LocalDateTime publishedAt;

    @ManyToOne
    @JoinColumn(name="seance_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Seance seance;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Note> notes;
}

