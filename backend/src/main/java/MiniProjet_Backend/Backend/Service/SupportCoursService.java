package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.SupportCours;
import MiniProjet_Backend.Backend.Repository.SupportCoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SupportCoursService {
    @Autowired
    private SupportCoursRepository supportCoursRepository;

    public List<SupportCours> getAllSupportCours() {
        return supportCoursRepository.findAll();
    }

    public Optional<SupportCours> getSupportCoursById(Integer id) {
        return supportCoursRepository.findById(id);
    }

    public List<SupportCours> getSupportCoursByEnseignement(Integer enseignementId) {
        return supportCoursRepository.findByEnseignementId(enseignementId);
    }

    public SupportCours saveSupportCours(SupportCours supportCours) {
        return supportCoursRepository.save(supportCours);
    }

    public SupportCours updateSupportCours(Integer id, SupportCours supportCoursDetails) {
        return supportCoursRepository.findById(id).map(supportCours -> {
            supportCours.setTitre(supportCoursDetails.getTitre());
            supportCours.setCheminFichier(supportCoursDetails.getCheminFichier());
            supportCours.setDateDepot(supportCoursDetails.getDateDepot());
            supportCours.setEnseignement(supportCoursDetails.getEnseignement());
            return supportCoursRepository.save(supportCours);
        }).orElseThrow(() -> new RuntimeException("SupportCours not found"));
    }

    public void deleteSupportCours(Integer id) {
        supportCoursRepository.deleteById(id);
    }
}

