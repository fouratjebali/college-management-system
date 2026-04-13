package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Presence;
import MiniProjet_Backend.Backend.Repository.PresenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PresenceService {
    @Autowired
    private PresenceRepository presenceRepository;

    public List<Presence> getAllPresences() {
        return presenceRepository.findAll();
    }

    public Optional<Presence> getPresenceById(Integer id) {
        return presenceRepository.findById(id);
    }

    public List<Presence> getPresencesByEtudiant(Integer etudiantId) {
        return presenceRepository.findByEtudiantId(etudiantId);
    }

    public List<Presence> getPresencesBySeance(Integer seanceId) {
        return presenceRepository.findBySeanceId(seanceId);
    }

    public Presence savePresence(Presence presence) {
        return presenceRepository.save(presence);
    }

    public Presence updatePresence(Integer id, Presence presenceDetails) {
        return presenceRepository.findById(id).map(presence -> {
            presence.setStatut(presenceDetails.getStatut());
            presence.setDateSaisie(presenceDetails.getDateSaisie());
            presence.setEtudiant(presenceDetails.getEtudiant());
            presence.setSeance(presenceDetails.getSeance());
            return presenceRepository.save(presence);
        }).orElseThrow(() -> new RuntimeException("Presence not found"));
    }

    public void deletePresence(Integer id) {
        presenceRepository.deleteById(id);
    }
}

