package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.AcademicYearRequestDTO;
import MiniProjet_Backend.Backend.DTO.AcademicYearResponseDTO;
import MiniProjet_Backend.Backend.DTO.SemesterRequestDTO;
import MiniProjet_Backend.Backend.Service.AcademicYearService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/academic-years")
public class AcademicYearController {
    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @GetMapping
    public ResponseEntity<List<AcademicYearResponseDTO>> getAcademicYears() {
        return ResponseEntity.ok(academicYearService.getAllAcademicYears());
    }

    @PostMapping
    public ResponseEntity<AcademicYearResponseDTO> createAcademicYear(
            @Valid @RequestBody AcademicYearRequestDTO request
    ) {
        return ResponseEntity.ok(academicYearService.createAcademicYear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicYearResponseDTO> updateAcademicYear(
            @PathVariable Integer id,
            @Valid @RequestBody AcademicYearRequestDTO request
    ) {
        return ResponseEntity.ok(academicYearService.updateAcademicYear(id, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<AcademicYearResponseDTO> activateAcademicYear(@PathVariable Integer id) {
        return ResponseEntity.ok(academicYearService.activateAcademicYear(id));
    }

    @PostMapping("/{academicYearId}/semesters")
    public ResponseEntity<AcademicYearResponseDTO> createSemester(
            @PathVariable Integer academicYearId,
            @Valid @RequestBody SemesterRequestDTO request
    ) {
        return ResponseEntity.ok(academicYearService.createSemester(academicYearId, request));
    }

    @PutMapping("/semesters/{semesterId}")
    public ResponseEntity<AcademicYearResponseDTO> updateSemester(
            @PathVariable Integer semesterId,
            @Valid @RequestBody SemesterRequestDTO request
    ) {
        return ResponseEntity.ok(academicYearService.updateSemester(semesterId, request));
    }

    @PatchMapping("/semesters/{semesterId}/activate")
    public ResponseEntity<AcademicYearResponseDTO> activateSemester(@PathVariable Integer semesterId) {
        return ResponseEntity.ok(academicYearService.activateSemester(semesterId));
    }

    @PatchMapping("/semesters/{semesterId}/lock")
    public ResponseEntity<AcademicYearResponseDTO> setSemesterLocked(
            @PathVariable Integer semesterId,
            @RequestParam boolean locked
    ) {
        return ResponseEntity.ok(academicYearService.setSemesterLocked(semesterId, locked));
    }
}
