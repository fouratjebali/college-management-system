package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.AnnonceRequestDTO;
import MiniProjet_Backend.Backend.DTO.AnnonceResponseDTO;
import MiniProjet_Backend.Backend.Model.Annonce;
import MiniProjet_Backend.Backend.Service.AnnonceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/api/annonces", "/api/announcements"})

public class AnnonceController {
    @Autowired
    private AnnonceService annonceService;

    @GetMapping
    public ResponseEntity<List<Annonce>> getAllAnnonces() {
        return ResponseEntity.ok(annonceService.getAllAnnonces());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<AnnonceResponseDTO>> getAllAnnoncesPaged(Pageable pageable) {
        return ResponseEntity.ok(annonceService.getAllAnnonces(pageable));
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

    @GetMapping("/administrateur/{administrateurId}/paged")
    public ResponseEntity<Page<AnnonceResponseDTO>> getAnnoncesByAdministrateurPaged(
            @PathVariable Integer administrateurId,
            Pageable pageable) {
        return ResponseEntity.ok(annonceService.getAnnoncesByAdministrateur(administrateurId, pageable));
    }

    @GetMapping("/global")
    public ResponseEntity<List<Annonce>> getGlobalAnnonces() {
        return ResponseEntity.ok(annonceService.getGlobalAnnonces());
    }

    @GetMapping("/active")
    public ResponseEntity<Page<AnnonceResponseDTO>> getActiveAnnonces(Pageable pageable) {
        return ResponseEntity.ok(annonceService.getActiveAnnonces(pageable));
    }

    @GetMapping("/visible")
    public ResponseEntity<Page<AnnonceResponseDTO>> getVisibleAnnonces(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            Pageable pageable) {
        return ResponseEntity.ok(annonceService.getVisibleAnnonces(role, activeOnly, pageable));
    }

    @PostMapping
    public ResponseEntity<Annonce> createAnnonce(@RequestBody Annonce annonce) {
        return ResponseEntity.ok(annonceService.saveAnnonce(annonce));
    }

    @PostMapping("/dto")
    public ResponseEntity<AnnonceResponseDTO> createAnnonceDto(@Valid @RequestBody AnnonceRequestDTO annonce) {
        return ResponseEntity.ok(annonceService.createAnnonce(annonce));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Annonce> updateAnnonce(@PathVariable Integer id, @RequestBody Annonce annonce) {
        return ResponseEntity.ok(annonceService.updateAnnonce(id, annonce));
    }

    @PutMapping("/{id}/dto")
    public ResponseEntity<AnnonceResponseDTO> updateAnnonceDto(
            @PathVariable Integer id,
            @Valid @RequestBody AnnonceRequestDTO annonce) {
        return ResponseEntity.ok(annonceService.updateAnnonce(id, annonce));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnonce(@PathVariable Integer id) {
        annonceService.deleteAnnonce(id);
        return ResponseEntity.noContent().build();
    }
}

