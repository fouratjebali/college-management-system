package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.ScheduleConflictDTO;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Repository.EnseignementRepository;
import MiniProjet_Backend.Backend.Repository.GroupeRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SeanceService {
    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private EnseignementRepository enseignementRepository;

    @Autowired
    private GroupeRepository groupeRepository;

    public List<Seance> getAllSeances() {
        return seanceRepository.findAll();
    }

    public Page<Seance> getAllSeances(Pageable pageable) {
        return seanceRepository.findAll(pageable);
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

    public Page<Seance> getSeancesByGroupe(Integer groupeId, Pageable pageable) {
        return seanceRepository.findByGroupeId(groupeId, pageable);
    }

    public Page<Seance> getSeancesByProfesseur(Integer professeurId, Pageable pageable) {
        return seanceRepository.findByEnseignementProfesseurId(professeurId, pageable);
    }

    public Page<Seance> getSeancesBySalle(String batiment, String salle, Pageable pageable) {
        return seanceRepository.findByBatimentIgnoreCaseAndSalleIgnoreCase(batiment, salle, pageable);
    }

    public Seance saveSeance(Seance seance) {
        attachManagedReferences(seance);
        validateNoConflicts(seance, null);
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
            attachManagedReferences(seance);
            validateNoConflicts(seance, id);
            return seanceRepository.save(seance);
        }).orElseThrow(() -> new RuntimeException("Seance not found"));
    }

    public void deleteSeance(Integer id) {
        seanceRepository.deleteById(id);
    }

    public List<ScheduleConflictDTO> findConflicts(Seance seance, Integer excludeId) {
        attachManagedReferences(seance);
        validateTimeRange(seance);

        List<ScheduleConflictDTO> conflicts = new ArrayList<>();
        Integer groupeId = seance.getGroupe().getId();
        Integer professeurId = seance.getEnseignement().getProfesseur().getId();

        seanceRepository.findGroupeConflicts(
                groupeId,
                seance.getJoursemaine(),
                seance.getHeureDebut(),
                seance.getHeureFin(),
                excludeId
        ).forEach(conflict -> conflicts.add(new ScheduleConflictDTO(
                "GROUPE",
                conflict.getId(),
                "Le groupe a déjà une séance sur ce créneau"
        )));

        seanceRepository.findSalleConflicts(
                seance.getBatiment(),
                seance.getSalle(),
                seance.getJoursemaine(),
                seance.getHeureDebut(),
                seance.getHeureFin(),
                excludeId
        ).forEach(conflict -> conflicts.add(new ScheduleConflictDTO(
                "SALLE",
                conflict.getId(),
                "La salle est déjà occupée sur ce créneau"
        )));

        seanceRepository.findProfesseurConflicts(
                professeurId,
                seance.getJoursemaine(),
                seance.getHeureDebut(),
                seance.getHeureFin(),
                excludeId
        ).forEach(conflict -> conflicts.add(new ScheduleConflictDTO(
                "PROFESSEUR",
                conflict.getId(),
                "Le professeur a déjà une séance sur ce créneau"
        )));

        return conflicts;
    }

    private void validateNoConflicts(Seance seance, Integer excludeId) {
        List<ScheduleConflictDTO> conflicts = findConflicts(seance, excludeId);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException(conflicts.get(0).getMessage());
        }
    }

    private void validateTimeRange(Seance seance) {
        if (seance.getHeureDebut() == null || seance.getHeureFin() == null) {
            throw new IllegalArgumentException("heureDebut et heureFin sont obligatoires");
        }
        if (!seance.getHeureDebut().isBefore(seance.getHeureFin())) {
            throw new IllegalArgumentException("heureDebut doit être avant heureFin");
        }
        if (seance.getJoursemaine() == null || seance.getJoursemaine().isBlank()) {
            throw new IllegalArgumentException("jourSemaine est obligatoire");
        }
        if (seance.getBatiment() == null || seance.getBatiment().isBlank()) {
            throw new IllegalArgumentException("batiment est obligatoire");
        }
        if (seance.getSalle() == null || seance.getSalle().isBlank()) {
            throw new IllegalArgumentException("salle est obligatoire");
        }
    }

    private void attachManagedReferences(Seance seance) {
        validateTimeRange(seance);
        if (seance.getGroupe() == null || seance.getGroupe().getId() == null) {
            throw new IllegalArgumentException("groupe.id est obligatoire");
        }
        if (seance.getEnseignement() == null || seance.getEnseignement().getId() == null) {
            throw new IllegalArgumentException("enseignement.id est obligatoire");
        }

        Groupe groupe = groupeRepository.findById(seance.getGroupe().getId())
                .orElseThrow(() -> new RuntimeException("Groupe not found"));
        Enseignement enseignement = enseignementRepository.findById(seance.getEnseignement().getId())
                .orElseThrow(() -> new RuntimeException("Enseignement not found"));
        if (enseignement.getProfesseur() == null || enseignement.getProfesseur().getId() == null) {
            throw new IllegalArgumentException("L'enseignement doit être associé à un professeur");
        }

        seance.setGroupe(groupe);
        seance.setEnseignement(enseignement);
    }
}

