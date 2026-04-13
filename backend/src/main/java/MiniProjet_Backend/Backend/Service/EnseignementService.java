package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Repository.EnseignementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EnseignementService {
    @Autowired
    private EnseignementRepository enseignementRepository;

    public List<Enseignement> getAllEnseignements() {
        return enseignementRepository.findAll();
    }

    public Optional<Enseignement> getEnseignementById(Integer id) {
        return enseignementRepository.findById(id);
    }

    public List<Enseignement> getEnseignementsByProfesseur(Integer professeurId) {
        return enseignementRepository.findByProfesseurId(professeurId);
    }

    public List<Enseignement> getEnseignementsByMatiere(Integer matiereId) {
        return enseignementRepository.findByMatiereId(matiereId);
    }

    public Enseignement saveEnseignement(Enseignement enseignement) {
        return enseignementRepository.save(enseignement);
    }

    public Enseignement updateEnseignement(Integer id, Enseignement enseignementDetails) {
        return enseignementRepository.findById(id).map(enseignement -> {
            enseignement.setSemestre(enseignementDetails.getSemestre());
            enseignement.setAnneeUniversitaire(enseignementDetails.getAnneeUniversitaire());
            enseignement.setProfesseur(enseignementDetails.getProfesseur());
            enseignement.setMatiere(enseignementDetails.getMatiere());
            return enseignementRepository.save(enseignement);
        }).orElseThrow(() -> new RuntimeException("Enseignement not found"));
    }

    public void deleteEnseignement(Integer id) {
        enseignementRepository.deleteById(id);
    }
}

