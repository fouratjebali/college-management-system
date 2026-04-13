package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.Matiere;
import MiniProjet_Backend.Backend.Service.MatiereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/matieres")

public class MatiereController {
    @Autowired
    private MatiereService matiereService;

    @GetMapping
    public ResponseEntity<List<Matiere>> getAllMatieres() {
        return ResponseEntity.ok(matiereService.getAllMatieres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Matiere> getMatiereById(@PathVariable Integer id) {
        return matiereService.getMatiereById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/departement/{departementId}")
    public ResponseEntity<List<Matiere>> getMatieresByDepartement(@PathVariable Integer departementId) {
        return ResponseEntity.ok(matiereService.getMatieresByDepartement(departementId));
    }

    @PostMapping
    public ResponseEntity<Matiere> createMatiere(@RequestBody Matiere matiere) {
        return ResponseEntity.ok(matiereService.saveMatiere(matiere));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Matiere> updateMatiere(@PathVariable Integer id, @RequestBody Matiere matiere) {
        return ResponseEntity.ok(matiereService.updateMatiere(id, matiere));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatiere(@PathVariable Integer id) {
        matiereService.deleteMatiere(id);
        return ResponseEntity.noContent().build();
    }
}

