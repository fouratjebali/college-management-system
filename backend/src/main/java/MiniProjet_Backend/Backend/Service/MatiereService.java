package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Matiere;
import MiniProjet_Backend.Backend.Repository.MatiereRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MatiereService {
    @Autowired
    private MatiereRepository matiereRepository;

    public List<Matiere> getAllMatieres() {
        return matiereRepository.findAll();
    }

    public Optional<Matiere> getMatiereById(Integer id) {
        return matiereRepository.findById(id);
    }

    public List<Matiere> getMatieresByDepartement(Integer departementId) {
        return matiereRepository.findByDepartementId(departementId);
    }

    public Matiere saveMatiere(Matiere matiere) {
        return matiereRepository.save(matiere);
    }

    public Matiere updateMatiere(Integer id, Matiere matiereDetails) {
        return matiereRepository.findById(id).map(matiere -> {
            matiere.setCode(matiereDetails.getCode());
            matiere.setLibelle(matiereDetails.getLibelle());
            matiere.setCoefficient(matiereDetails.getCoefficient());
            matiere.setDepartement(matiereDetails.getDepartement());
            return matiereRepository.save(matiere);
        }).orElseThrow(() -> new RuntimeException("Matiere not found"));
    }

    public void deleteMatiere(Integer id) {
        matiereRepository.deleteById(id);
    }
}

