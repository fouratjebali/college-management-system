package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatiereRepository extends JpaRepository<Matiere, Integer> {
    List<Matiere> findByDepartementId(Integer departementId);
}

