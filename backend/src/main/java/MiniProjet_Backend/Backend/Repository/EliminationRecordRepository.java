package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.EliminationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EliminationRecordRepository extends JpaRepository<EliminationRecord, Integer> {
    Optional<EliminationRecord> findByEtudiantIdAndMatiereIdAndTypeSeanceIgnoreCase(
            Integer etudiantId,
            Integer matiereId,
            String typeSeance
    );

    List<EliminationRecord> findByEtudiantId(Integer etudiantId);
}
