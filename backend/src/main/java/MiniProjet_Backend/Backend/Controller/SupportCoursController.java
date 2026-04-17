package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.SupportCoursResponseDTO;
import MiniProjet_Backend.Backend.Model.SupportCours;
import MiniProjet_Backend.Backend.Service.SupportCoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping({"/api/supports", "/api/course-materials"})

public class SupportCoursController {
    @Autowired
    private SupportCoursService supportCoursService;

    @GetMapping
    public ResponseEntity<List<SupportCours>> getAllSupportCours() {
        return ResponseEntity.ok(supportCoursService.getAllSupportCours());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<SupportCoursResponseDTO>> getAllSupportCoursPaged(Pageable pageable) {
        return ResponseEntity.ok(supportCoursService.getAllSupportCours(pageable));
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

    @GetMapping("/enseignement/{enseignementId}/paged")
    public ResponseEntity<Page<SupportCoursResponseDTO>> getSupportCoursByEnseignementPaged(
            @PathVariable Integer enseignementId,
            Pageable pageable) {
        return ResponseEntity.ok(supportCoursService.getSupportCoursByEnseignement(enseignementId, pageable));
    }

    @PostMapping
    public ResponseEntity<SupportCours> createSupportCours(@RequestBody SupportCours supportCours) {
        return ResponseEntity.ok(supportCoursService.saveSupportCours(supportCours));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SupportCoursResponseDTO> uploadSupportCours(
            @RequestParam String titre,
            @RequestParam Integer enseignementId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(supportCoursService.uploadSupportCours(titre, enseignementId, file));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadSupportCours(@PathVariable Integer id) {
        SupportCours supportCours = supportCoursService.getSupportCoursById(id)
                .orElseThrow(() -> new RuntimeException("SupportCours not found"));
        Resource resource = supportCoursService.loadSupportFile(id);
        String fileName = URLEncoder.encode(
                supportCours.getNomFichierOriginal(),
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(supportCours.getTypeFichier()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .body(resource);
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

