package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.Model.Note;
import MiniProjet_Backend.Backend.Service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notes")

public class NoteController {
    @Autowired
    private NoteService noteService;

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Integer id) {
        return noteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/evaluation/{evaluationId}")
    public ResponseEntity<List<Note>> getNotesByEvaluation(@PathVariable Integer evaluationId) {
        return ResponseEntity.ok(noteService.getNotesByEvaluation(evaluationId));
    }

    @GetMapping("/etudiant/{etudiantId}")
    public ResponseEntity<List<Note>> getNotesByEtudiant(@PathVariable Integer etudiantId) {
        return ResponseEntity.ok(noteService.getNotesByEtudiant(etudiantId));
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        return ResponseEntity.ok(noteService.saveNote(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Integer id, @RequestBody Note note) {
        return ResponseEntity.ok(noteService.updateNote(id, note));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Integer id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}

