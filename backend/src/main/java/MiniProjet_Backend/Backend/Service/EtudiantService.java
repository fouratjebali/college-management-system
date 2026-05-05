package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EtudiantService {
    @Autowired
    private EtudiantRepository etudiantRepository;

    public List<Etudiant> getAllEtudiants() {
        return etudiantRepository.findAll();
    }

    public Optional<Etudiant> getEtudiantById(Integer id) {
        return etudiantRepository.findById(id);
    }

    public Optional<Etudiant> getEtudiantByMatricule(String matricule) {
        return etudiantRepository.findByMatricule(matricule);
    }

    public List<Etudiant> getEtudiantsByGroupe(Integer groupeId) {
        return etudiantRepository.findByGroupeId(groupeId);
    }

    public Etudiant saveEtudiant(Etudiant etudiant) {
        return etudiantRepository.save(etudiant);
    }

    public Etudiant updateEtudiant(Integer id, Etudiant etudiantDetails) {
        return etudiantRepository.findById(id).map(etudiant -> {
            etudiant.setNomComplet(etudiantDetails.getNomComplet());
            etudiant.setEmail(etudiantDetails.getEmail());
            etudiant.setMotDePasseHash(etudiantDetails.getMotDePasseHash());
            etudiant.setActif(etudiantDetails.isActif());
            etudiant.setMatricule(etudiantDetails.getMatricule());
            etudiant.setNiveau(etudiantDetails.getNiveau());
            etudiant.setGroupe(etudiantDetails.getGroupe());
            return etudiantRepository.save(etudiant);
        }).orElseThrow(() -> new RuntimeException("Etudiant not found"));
    }

    public void deleteEtudiant(Integer id) {
        etudiantRepository.deleteById(id);
    }
}

