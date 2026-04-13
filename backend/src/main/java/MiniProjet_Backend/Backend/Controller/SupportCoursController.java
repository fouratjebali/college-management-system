package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.SupportCours;
import MiniProjet_Backend.Backend.Service.SupportCoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/supports")

public class SupportCoursController {
    @Autowired
    private SupportCoursService supportCoursService;

    @GetMapping
    public ResponseEntity<List<SupportCours>> getAllSupportCours() {
        return ResponseEntity.ok(supportCoursService.getAllSupportCours());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupportCours> getSupportCoursById(@PathVariable Integer id) {
        return supportCoursService.getSupportCoursById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/enseignement/{enseignementId}")
    public ResponseEntity<List<SupportCours>> getSupportCoursByEnseignement(@PathVariable Integer enseignementId) {
        return ResponseEntity.ok(supportCoursService.getSupportCoursByEnseignement(enseignementId));
    }

    @PostMapping
    public ResponseEntity<SupportCours> createSupportCours(@RequestBody SupportCours supportCours) {
        return ResponseEntity.ok(supportCoursService.saveSupportCours(supportCours));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupportCours> updateSupportCours(@PathVariable Integer id, @RequestBody SupportCours supportCours) {
        return ResponseEntity.ok(supportCoursService.updateSupportCours(id, supportCours));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupportCours(@PathVariable Integer id) {
        supportCoursService.deleteSupportCours(id);
        return ResponseEntity.noContent().build();
    }
}

