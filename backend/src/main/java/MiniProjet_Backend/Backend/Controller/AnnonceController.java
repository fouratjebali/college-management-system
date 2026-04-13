package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.Annonce;
import MiniProjet_Backend.Backend.Service.AnnonceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/annonces")

public class AnnonceController {
    @Autowired
    private AnnonceService annonceService;

    @GetMapping
    public ResponseEntity<List<Annonce>> getAllAnnonces() {
        return ResponseEntity.ok(annonceService.getAllAnnonces());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Annonce> getAnnonceById(@PathVariable Integer id) {
        return annonceService.getAnnonceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/administrateur/{administrateurId}")
    public ResponseEntity<List<Annonce>> getAnnoncesByAdministrateur(@PathVariable Integer administrateurId) {
        return ResponseEntity.ok(annonceService.getAnnoncesByAdministrateur(administrateurId));
    }

    @GetMapping("/global")
    public ResponseEntity<List<Annonce>> getGlobalAnnonces() {
        return ResponseEntity.ok(annonceService.getGlobalAnnonces());
    }

    @PostMapping
    public ResponseEntity<Annonce> createAnnonce(@RequestBody Annonce annonce) {
        return ResponseEntity.ok(annonceService.saveAnnonce(annonce));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Annonce> updateAnnonce(@PathVariable Integer id, @RequestBody Annonce annonce) {
        return ResponseEntity.ok(annonceService.updateAnnonce(id, annonce));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnonce(@PathVariable Integer id) {
        annonceService.deleteAnnonce(id);
        return ResponseEntity.noContent().build();
    }
}

