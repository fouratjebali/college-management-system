package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.SupportCours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupportCoursRepository extends JpaRepository<SupportCours, Integer> {
    List<SupportCours> findByEnseignementId(Integer enseignementId);
}

