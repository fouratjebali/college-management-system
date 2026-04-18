package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name="annonce")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Annonce {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(nullable = false)
    private LocalDateTime datePublication;

    @Column(nullable = false)
    private LocalDateTime dateExpiration;

    @Column(nullable = false)
    private Boolean cibleGlobale;

    @Column(length = 30)
    private String cibleRole;

    @ManyToOne
    @JoinColumn(name="administrateur_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Administrateur administrateur;

    @PrePersist
    void beforeCreate() {
        if (datePublication == null) {
            datePublication = LocalDateTime.now();
        }
        if (cibleGlobale == null) {
            cibleGlobale = Boolean.TRUE;
        }
    }
}

