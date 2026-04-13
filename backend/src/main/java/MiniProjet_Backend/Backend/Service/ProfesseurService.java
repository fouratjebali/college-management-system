package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Professeur;
import MiniProjet_Backend.Backend.Repository.ProfesseurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProfesseurService {
    @Autowired
    private ProfesseurRepository professeurRepository;

    public List<Professeur> getAllProfesseurs() {
        return professeurRepository.findAll();
    }

    public Optional<Professeur> getProfesseurById(Integer id) {
        return professeurRepository.findById(id);
    }

    public Optional<Professeur> getProfesseurByMatricule(String matricule) {
        return professeurRepository.findByMatriculePro(matricule);
    }

    public Professeur saveProfesseur(Professeur professeur) {
        return professeurRepository.save(professeur);
    }

    public Professeur updateProfesseur(Integer id, Professeur professeurDetails) {
        return professeurRepository.findById(id).map(professeur -> {
            professeur.setNomComplet(professeurDetails.getNomComplet());
            professeur.setEmail(professeurDetails.getEmail());
            professeur.setMotDePasseHash(professeurDetails.getMotDePasseHash());
            professeur.setActif(professeurDetails.isActif());
            professeur.setMatriculePro(professeurDetails.getMatriculePro());
            professeur.setGrade(professeurDetails.getGrade());
            return professeurRepository.save(professeur);
        }).orElseThrow(() -> new RuntimeException("Professeur not found"));
    }

    public void deleteProfesseur(Integer id) {
        professeurRepository.deleteById(id);
    }
}

