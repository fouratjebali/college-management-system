package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.Presence;
import MiniProjet_Backend.Backend.Service.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/presences")

public class PresenceController {
    @Autowired
    private PresenceService presenceService;

    @GetMapping
    public ResponseEntity<List<Presence>> getAllPresences() {
        return ResponseEntity.ok(presenceService.getAllPresences());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Presence> getPresenceById(@PathVariable Integer id) {
        return presenceService.getPresenceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/etudiant/{etudiantId}")
    public ResponseEntity<List<Presence>> getPresencesByEtudiant(@PathVariable Integer etudiantId) {
        return ResponseEntity.ok(presenceService.getPresencesByEtudiant(etudiantId));
    }

    @GetMapping("/seance/{seanceId}")
    public ResponseEntity<List<Presence>> getPresencesBySeance(@PathVariable Integer seanceId) {
        return ResponseEntity.ok(presenceService.getPresencesBySeance(seanceId));
    }

    @PostMapping
    public ResponseEntity<Presence> createPresence(@RequestBody Presence presence) {
        return ResponseEntity.ok(presenceService.savePresence(presence));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Presence> updatePresence(@PathVariable Integer id, @RequestBody Presence presence) {
        return ResponseEntity.ok(presenceService.updatePresence(id, presence));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePresence(@PathVariable Integer id) {
        presenceService.deletePresence(id);
        return ResponseEntity.noContent().build();
    }
}

