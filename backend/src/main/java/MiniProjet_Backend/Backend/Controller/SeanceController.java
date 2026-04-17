package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Service.SeanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seances")

public class SeanceController {
    @Autowired
    private SeanceService seanceService;

    @GetMapping
    public ResponseEntity<List<Seance>> getAllSeances() {
        return ResponseEntity.ok(seanceService.getAllSeances());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Seance>> getAllSeancesPaged(Pageable pageable) {
        return ResponseEntity.ok(seanceService.getAllSeances(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seance> getSeanceById(@PathVariable Integer id) {
        return seanceService.getSeanceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/enseignement/{enseignementId}")
    public ResponseEntity<List<Seance>> getSeancesByEnseignement(@PathVariable Integer enseignementId) {
        return ResponseEntity.ok(seanceService.getSeancesByEnseignement(enseignementId));
    }

    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<List<Seance>> getSeancesByGroupe(@PathVariable Integer groupeId) {
        return ResponseEntity.ok(seanceService.getSeancesByGroupe(groupeId));
    }

    @GetMapping("/groupe/{groupeId}/paged")
    public ResponseEntity<Page<Seance>> getSeancesByGroupePaged(
            @PathVariable Integer groupeId,
            Pageable pageable) {
        return ResponseEntity.ok(seanceService.getSeancesByGroupe(groupeId, pageable));
    }

    @PostMapping
    public ResponseEntity<Seance> createSeance(@RequestBody Seance seance) {
        return ResponseEntity.ok(seanceService.saveSeance(seance));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Seance> updateSeance(@PathVariable Integer id, @RequestBody Seance seance) {
        return ResponseEntity.ok(seanceService.updateSeance(id, seance));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeance(@PathVariable Integer id) {
        seanceService.deleteSeance(id);
        return ResponseEntity.noContent().build();
    }
}

