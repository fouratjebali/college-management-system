package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Integer> {
    List<Seance> findByEnseignementId(Integer enseignementId);
    List<Seance> findByGroupeId(Integer groupeId);
}

