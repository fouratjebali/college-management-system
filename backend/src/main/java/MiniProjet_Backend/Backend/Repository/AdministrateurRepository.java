package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdministrateurRepository extends JpaRepository<Administrateur, Integer> {
    Optional<Administrateur> findByMatriculeAdmin(String matriculeAdmin);
}

