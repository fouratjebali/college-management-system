package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name="support_cours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportCours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String cheminFichier;

    @Column(nullable = false)
    private String nomFichierOriginal;

    @Column(nullable = false)
    private String typeFichier;

    @Column(nullable = false)
    private Long tailleOctets;

    @Column(nullable = false)
    private LocalDateTime dateDepot;

    @ManyToOne
    @JoinColumn(name="enseignement_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Enseignement enseignement;

    @PrePersist
    void beforeCreate() {
        if (dateDepot == null) {
            dateDepot = LocalDateTime.now();
        }
    }
}

