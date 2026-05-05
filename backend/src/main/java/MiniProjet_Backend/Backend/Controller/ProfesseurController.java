package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.Professeur;
import MiniProjet_Backend.Backend.Service.ProfesseurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/professeurs")

public class ProfesseurController {
    @Autowired
    private ProfesseurService professeurService;

    @GetMapping
    public ResponseEntity<List<Professeur>> getAllProfesseurs() {
        return ResponseEntity.ok(professeurService.getAllProfesseurs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professeur> getProfesseurById(@PathVariable Integer id) {
        return professeurService.getProfesseurById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<Professeur> getProfesseurByMatricule(@PathVariable String matricule) {
        return professeurService.getProfesseurByMatricule(matricule)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Professeur> createProfesseur(@RequestBody Professeur professeur) {
        return ResponseEntity.ok(professeurService.saveProfesseur(professeur));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professeur> updateProfesseur(@PathVariable Integer id, @RequestBody Professeur professeur) {
        return ResponseEntity.ok(professeurService.updateProfesseur(id, professeur));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfesseur(@PathVariable Integer id) {
        professeurService.deleteProfesseur(id);
        return ResponseEntity.noContent().build();
    }
}

