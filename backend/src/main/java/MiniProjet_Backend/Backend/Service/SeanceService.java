package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SeanceService {
    @Autowired
    private SeanceRepository seanceRepository;

    public List<Seance> getAllSeances() {
        return seanceRepository.findAll();
    }

    public Optional<Seance> getSeanceById(Integer id) {
        return seanceRepository.findById(id);
    }

    public List<Seance> getSeancesByEnseignement(Integer enseignementId) {
        return seanceRepository.findByEnseignementId(enseignementId);
    }

    public List<Seance> getSeancesByGroupe(Integer groupeId) {
        return seanceRepository.findByGroupeId(groupeId);
    }

    public Seance saveSeance(Seance seance) {
        return seanceRepository.save(seance);
    }

    public Seance updateSeance(Integer id, Seance seanceDetails) {
        return seanceRepository.findById(id).map(seance -> {
            seance.setTypeSeance(seanceDetails.getTypeSeance());
            seance.setJoursemaine(seanceDetails.getJoursemaine());
            seance.setHeureDebut(seanceDetails.getHeureDebut());
            seance.setHeureFin(seanceDetails.getHeureFin());
            seance.setSalle(seanceDetails.getSalle());
            seance.setBatiment(seanceDetails.getBatiment());
            seance.setEnseignement(seanceDetails.getEnseignement());
            seance.setGroupe(seanceDetails.getGroupe());
            return seanceRepository.save(seance);
        }).orElseThrow(() -> new RuntimeException("Seance not found"));
    }

    public void deleteSeance(Integer id) {
        seanceRepository.deleteById(id);
    }
}

