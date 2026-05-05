package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Service.GroupeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/groupes")

public class GroupeController {
    @Autowired
    private GroupeService groupeService;

    @GetMapping
    public ResponseEntity<List<Groupe>> getAllGroupes() {
        return ResponseEntity.ok(groupeService.getAllGroupes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Groupe> getGroupeById(@PathVariable Integer id) {
        return groupeService.getGroupeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/departement/{departementId}")
    public ResponseEntity<List<Groupe>> getGroupesByDepartement(@PathVariable Integer departementId) {
        return ResponseEntity.ok(groupeService.getGroupesByDepartement(departementId));
    }

    @PostMapping
    public ResponseEntity<Groupe> createGroupe(@RequestBody Groupe groupe) {
        return ResponseEntity.ok(groupeService.saveGroupe(groupe));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Groupe> updateGroupe(@PathVariable Integer id, @RequestBody Groupe groupe) {
        return ResponseEntity.ok(groupeService.updateGroupe(id, groupe));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroupe(@PathVariable Integer id) {
        groupeService.deleteGroupe(id);
        return ResponseEntity.noContent().build();
    }
}

