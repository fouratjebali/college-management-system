package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Annonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Integer> {
    List<Annonce> findByAdministrateurId(Integer administrateurId);
    List<Annonce> findByCibleGlobaleTrue();
}

