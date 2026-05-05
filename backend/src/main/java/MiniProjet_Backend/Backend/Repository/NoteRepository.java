package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {
    List<Note> findByEvaluationId(Integer evaluationId);
    List<Note> findByEtudiantId(Integer etudiantId);
    Optional<Note> findByEvaluationIdAndEtudiantId(Integer evaluationId, Integer etudiantId);
}

