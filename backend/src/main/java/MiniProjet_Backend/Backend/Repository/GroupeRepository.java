package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Groupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupeRepository extends JpaRepository<Groupe, Integer> {
    List<Groupe> findByDepartementId(Integer departementId);
}

