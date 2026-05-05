package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Integer> {
    List<Presence> findByEtudiantId(Integer etudiantId);
    List<Presence> findBySeanceId(Integer seanceId);
    Optional<Presence> findBySeanceIdAndEtudiantId(Integer seanceId, Integer etudiantId);
}

