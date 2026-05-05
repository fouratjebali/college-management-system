package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Service.EnseignementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/enseignements")

public class EnseignementController {
    @Autowired
    private EnseignementService enseignementService;

    @GetMapping
    public ResponseEntity<List<Enseignement>> getAllEnseignements() {
        return ResponseEntity.ok(enseignementService.getAllEnseignements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enseignement> getEnseignementById(@PathVariable Integer id) {
        return enseignementService.getEnseignementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/professeur/{professeurId}")
    public ResponseEntity<List<Enseignement>> getEnseignementsByProfesseur(@PathVariable Integer professeurId) {
        return ResponseEntity.ok(enseignementService.getEnseignementsByProfesseur(professeurId));
    }

    @GetMapping("/matiere/{matiereId}")
    public ResponseEntity<List<Enseignement>> getEnseignementsByMatiere(@PathVariable Integer matiereId) {
        return ResponseEntity.ok(enseignementService.getEnseignementsByMatiere(matiereId));
    }

    @PostMapping
    public ResponseEntity<Enseignement> createEnseignement(@RequestBody Enseignement enseignement) {
        return ResponseEntity.ok(enseignementService.saveEnseignement(enseignement));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Enseignement> updateEnseignement(@PathVariable Integer id, @RequestBody Enseignement enseignement) {
        return ResponseEntity.ok(enseignementService.updateEnseignement(id, enseignement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnseignement(@PathVariable Integer id) {
        enseignementService.deleteEnseignement(id);
        return ResponseEntity.noContent().build();
    }
}

