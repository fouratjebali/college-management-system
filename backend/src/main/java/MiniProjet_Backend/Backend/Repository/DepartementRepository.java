package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Integer> {
    Optional<Departement> findByNom(String nom);
}

