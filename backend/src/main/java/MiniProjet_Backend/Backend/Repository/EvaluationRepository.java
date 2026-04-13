package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {
    List<Evaluation> findBySeanceId(Integer seanceId);
}

