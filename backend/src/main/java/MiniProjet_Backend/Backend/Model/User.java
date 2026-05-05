package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="utilisateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nomComplet;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasseHash;

    @Column(nullable = false)
    private boolean actif;

}
