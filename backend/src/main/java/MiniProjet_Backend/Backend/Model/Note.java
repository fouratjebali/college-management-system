package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name="note")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Float valeur;

    @Column(nullable = false)
    private String statut;

    @Column
    private String remarque;

    @Column
    private String validationRemark;

    @Column
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime validatedAt;

    @Column
    private LocalDateTime publishedAt;

    @ManyToOne
    @JoinColumn(name="evaluation_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Evaluation evaluation;

    @ManyToOne
    @JoinColumn(name="etudiant_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Etudiant etudiant;
}

