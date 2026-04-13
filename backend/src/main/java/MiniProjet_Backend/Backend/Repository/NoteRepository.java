package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {
    List<Note> findByEvaluationId(Integer evaluationId);
    List<Note> findByEtudiantId(Integer etudiantId);
}

