package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.Departement;
import MiniProjet_Backend.Backend.Service.DepartementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departements")

public class DepartementController {
    @Autowired
    private DepartementService departementService;

    @GetMapping
    public ResponseEntity<List<Departement>> getAllDepartements() {
        return ResponseEntity.ok(departementService.getAllDepartements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departement> getDepartementById(@PathVariable Integer id) {
        return departementService.getDepartementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Departement> createDepartement(@RequestBody Departement departement) {
        return ResponseEntity.ok(departementService.saveDepartement(departement));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Departement> updateDepartement(@PathVariable Integer id, @RequestBody Departement departement) {
        return ResponseEntity.ok(departementService.updateDepartement(id, departement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartement(@PathVariable Integer id) {
        departementService.deleteDepartement(id);
        return ResponseEntity.noContent().build();
    }
}

