package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProfesseurRepository extends JpaRepository<Professeur, Integer> {
    Optional<Professeur> findByMatriculePro(String matriculePro);
    Optional<Professeur> findByEmail(String email);
}

