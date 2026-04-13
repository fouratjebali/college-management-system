package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Annonce;
import MiniProjet_Backend.Backend.Repository.AnnonceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AnnonceService {
    @Autowired
    private AnnonceRepository annonceRepository;

    public List<Annonce> getAllAnnonces() {
        return annonceRepository.findAll();
    }

    public Optional<Annonce> getAnnonceById(Integer id) {
        return annonceRepository.findById(id);
    }

    public List<Annonce> getAnnoncesByAdministrateur(Integer administrateurId) {
        return annonceRepository.findByAdministrateurId(administrateurId);
    }

    public List<Annonce> getGlobalAnnonces() {
        return annonceRepository.findByCibleGlobaleTrue();
    }

    public Annonce saveAnnonce(Annonce annonce) {
        return annonceRepository.save(annonce);
    }

    public Annonce updateAnnonce(Integer id, Annonce annonceDetails) {
        return annonceRepository.findById(id).map(annonce -> {
            annonce.setTitre(annonceDetails.getTitre());
            annonce.setContenu(annonceDetails.getContenu());
            annonce.setDatePublication(annonceDetails.getDatePublication());
            annonce.setDateExpiration(annonceDetails.getDateExpiration());
            annonce.setCibleGlobale(annonceDetails.getCibleGlobale());
            annonce.setAdministrateur(annonceDetails.getAdministrateur());
            return annonceRepository.save(annonce);
        }).orElseThrow(() -> new RuntimeException("Annonce not found"));
    }

    public void deleteAnnonce(Integer id) {
        annonceRepository.deleteById(id);
    }
}

