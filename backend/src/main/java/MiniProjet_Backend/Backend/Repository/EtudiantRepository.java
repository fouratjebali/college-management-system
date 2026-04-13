package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Integer> {
    Optional<Etudiant> findByMatricule(String matricule);
    List<Etudiant> findByGroupeId(Integer groupeId);
}

