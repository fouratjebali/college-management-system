package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Note;
import MiniProjet_Backend.Backend.Repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    @Autowired
    private NoteRepository noteRepository;

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Optional<Note> getNoteById(Integer id) {
        return noteRepository.findById(id);
    }

    public List<Note> getNotesByEvaluation(Integer evaluationId) {
        return noteRepository.findByEvaluationId(evaluationId);
    }

    public List<Note> getNotesByEtudiant(Integer etudiantId) {
        return noteRepository.findByEtudiantId(etudiantId);
    }

    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    public Note updateNote(Integer id, Note noteDetails) {
        return noteRepository.findById(id).map(note -> {
            note.setValeur(noteDetails.getValeur());
            note.setStatut(noteDetails.getStatut());
            note.setRemarque(noteDetails.getRemarque());
            note.setEvaluation(noteDetails.getEvaluation());
            note.setEtudiant(noteDetails.getEtudiant());
            return noteRepository.save(note);
        }).orElseThrow(() -> new RuntimeException("Note not found"));
    }

    public void deleteNote(Integer id) {
        noteRepository.deleteById(id);
    }
}

