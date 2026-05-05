package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Presence;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import MiniProjet_Backend.Backend.Repository.PresenceRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PresenceService {
    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

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
        Integer seanceId = presence.getSeance() == null ? null : presence.getSeance().getId();
        Integer studentId = presence.getEtudiant() == null ? null : presence.getEtudiant().getId();

        if (seanceId == null || studentId == null) {
            return presenceRepository.save(presence);
        }

        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Seance not found"));
        if ("CLOTUREE".equalsIgnoreCase(seance.getAttendanceStatus())) {
            throw new RuntimeException("Cette seance de presence est cloturee.");
        }

        return presenceRepository.findBySeanceIdAndEtudiantId(seanceId, studentId)
                .map(existingPresence -> {
                    existingPresence.setStatut(presence.getStatut());
                    existingPresence.setDateSaisie(presence.getDateSaisie());
                    existingPresence.setSeance(seance);
                    existingPresence.setEtudiant(etudiantRepository.findById(studentId)
                            .orElseThrow(() -> new RuntimeException("Etudiant not found")));
                    return presenceRepository.save(existingPresence);
                })
                .orElseGet(() -> {
                    presence.setSeance(seance);
                    presence.setEtudiant(etudiantRepository.findById(studentId)
                            .orElseThrow(() -> new RuntimeException("Etudiant not found")));
                    return presenceRepository.save(presence);
                });
    }

    public Presence updatePresence(Integer id, Presence presenceDetails) {
        return presenceRepository.findById(id).map(presence -> {
            if ("CLOTUREE".equalsIgnoreCase(presence.getSeance().getAttendanceStatus())) {
                throw new RuntimeException("Cette seance de presence est cloturee.");
            }
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

