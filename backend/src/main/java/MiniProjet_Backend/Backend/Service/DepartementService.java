package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Departement;
import MiniProjet_Backend.Backend.Repository.DepartementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DepartementService {
    @Autowired
    private DepartementRepository departementRepository;

    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }

    public Optional<Departement> getDepartementById(Integer id) {
        return departementRepository.findById(id);
    }

    public Departement saveDepartement(Departement departement) {
        return departementRepository.save(departement);
    }

    public Departement updateDepartement(Integer id, Departement departementDetails) {
        return departementRepository.findById(id).map(departement -> {
            departement.setNom(departementDetails.getNom());
            return departementRepository.save(departement);
        }).orElseThrow(() -> new RuntimeException("Departement not found"));
    }

    public void deleteDepartement(Integer id) {
        departementRepository.deleteById(id);
    }
}

