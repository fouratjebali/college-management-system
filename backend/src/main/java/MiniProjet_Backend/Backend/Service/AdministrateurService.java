package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Administrateur;
import MiniProjet_Backend.Backend.Repository.AdministrateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AdministrateurService {
    @Autowired
    private AdministrateurRepository administrateurRepository;

    public List<Administrateur> getAllAdministrateurs() {
        return administrateurRepository.findAll();
    }

    public Optional<Administrateur> getAdministrateurById(Integer id) {
        return administrateurRepository.findById(id);
    }

    public Optional<Administrateur> getAdministrateurByMatricule(String matricule) {
        return administrateurRepository.findByMatriculeAdmin(matricule);
    }

    public Administrateur saveAdministrateur(Administrateur admin) {
        return administrateurRepository.save(admin);
    }

    public Administrateur updateAdministrateur(Integer id, Administrateur adminDetails) {
        return administrateurRepository.findById(id).map(admin -> {
            admin.setNomComplet(adminDetails.getNomComplet());
            admin.setEmail(adminDetails.getEmail());
            admin.setMotDePasseHash(adminDetails.getMotDePasseHash());
            admin.setActif(adminDetails.isActif());
            admin.setMatriculeAdmin(adminDetails.getMatriculeAdmin());
            admin.setFonction(adminDetails.getFonction());
            return administrateurRepository.save(admin);
        }).orElseThrow(() -> new RuntimeException("Administrateur not found"));
    }

    public void deleteAdministrateur(Integer id) {
        administrateurRepository.deleteById(id);
    }
}

