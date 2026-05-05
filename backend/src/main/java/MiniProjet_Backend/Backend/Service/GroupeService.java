package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Repository.GroupeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GroupeService {
    @Autowired
    private GroupeRepository groupeRepository;

    public List<Groupe> getAllGroupes() {
        return groupeRepository.findAll();
    }

    public Optional<Groupe> getGroupeById(Integer id) {
        return groupeRepository.findById(id);
    }

    public List<Groupe> getGroupesByDepartement(Integer departementId) {
        return groupeRepository.findByDepartementId(departementId);
    }

    public Groupe saveGroupe(Groupe groupe) {
        return groupeRepository.save(groupe);
    }

    public Groupe updateGroupe(Integer id, Groupe groupeDetails) {
        return groupeRepository.findById(id).map(groupe -> {
            groupe.setLibelle(groupeDetails.getLibelle());
            groupe.setNiveau(groupeDetails.getNiveau());
            groupe.setAnneeUniversitaire(groupeDetails.getAnneeUniversitaire());
            groupe.setDepartement(groupeDetails.getDepartement());
            return groupeRepository.save(groupe);
        }).orElseThrow(() -> new RuntimeException("Groupe not found"));
    }

    public void deleteGroupe(Integer id) {
        groupeRepository.deleteById(id);
    }
}

