package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.ScheduleConflictDTO;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Service.SeanceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
    private final SeanceService seanceService;

    public ScheduleController(SeanceService seanceService) {
        this.seanceService = seanceService;
    }

    @GetMapping
    public ResponseEntity<Page<Seance>> getSchedules(Pageable pageable) {
        return ResponseEntity.ok(seanceService.getAllSeances(pageable));
    }

    @GetMapping("/group/{groupeId}")
    public ResponseEntity<Page<Seance>> getSchedulesByGroup(
            @PathVariable Integer groupeId,
            Pageable pageable) {
        return ResponseEntity.ok(seanceService.getSeancesByGroupe(groupeId, pageable));
    }

    @GetMapping("/professor/{professeurId}")
    public ResponseEntity<Page<Seance>> getSchedulesByProfessor(
            @PathVariable Integer professeurId,
            Pageable pageable) {
        return ResponseEntity.ok(seanceService.getSeancesByProfesseur(professeurId, pageable));
    }

    @GetMapping("/room")
    public ResponseEntity<Page<Seance>> getSchedulesByRoom(
            @RequestParam String batiment,
            @RequestParam String salle,
            Pageable pageable) {
        return ResponseEntity.ok(seanceService.getSeancesBySalle(batiment, salle, pageable));
    }

    @PostMapping("/validate")
    public ResponseEntity<List<ScheduleConflictDTO>> validateSchedule(@RequestBody Seance seance) {
        return ResponseEntity.ok(seanceService.findConflicts(seance, seance.getId()));
    }

    @PostMapping
    public ResponseEntity<Seance> createSchedule(@RequestBody Seance seance) {
        return ResponseEntity.ok(seanceService.saveSeance(seance));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Seance> updateSchedule(
            @PathVariable Integer id,
            @RequestBody Seance seance) {
        return ResponseEntity.ok(seanceService.updateSeance(id, seance));
    }
}
