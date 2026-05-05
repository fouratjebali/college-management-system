package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Enseignement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EnseignementRepository extends JpaRepository<Enseignement, Integer> {
    List<Enseignement> findByProfesseurId(Integer professeurId);
    List<Enseignement> findByMatiereId(Integer matiereId);
}

