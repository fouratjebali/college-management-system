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

    @Autowired
    private NoteWorkflowService noteWorkflowService;

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
        if (note.getEvaluation() != null
                && note.getEvaluation().getId() != null
                && note.getEtudiant() != null
                && note.getEtudiant().getId() != null) {
            return noteRepository
                    .findByEvaluationIdAndEtudiantId(note.getEvaluation().getId(), note.getEtudiant().getId())
                    .map(existingNote -> {
                        existingNote.setValeur(note.getValeur());
                        existingNote.setRemarque(note.getRemarque());
                        existingNote.setEvaluation(note.getEvaluation());
                        existingNote.setEtudiant(note.getEtudiant());
                        noteWorkflowService.markSubmitted(existingNote, note.getStatut());
                        return noteRepository.save(existingNote);
                    })
                    .orElseGet(() -> {
                        noteWorkflowService.markSubmitted(note, note.getStatut());
                        return noteRepository.save(note);
                    });
        }

        noteWorkflowService.markSubmitted(note, note.getStatut());
        return noteRepository.save(note);
    }

    public Note updateNote(Integer id, Note noteDetails) {
        return noteRepository.findById(id).map(note -> {
            note.setValeur(noteDetails.getValeur());
            note.setRemarque(noteDetails.getRemarque());
            note.setEvaluation(noteDetails.getEvaluation());
            note.setEtudiant(noteDetails.getEtudiant());
            noteWorkflowService.markSubmitted(note, noteDetails.getStatut());
            return noteRepository.save(note);
        }).orElseThrow(() -> new RuntimeException("Note not found"));
    }

    public void deleteNote(Integer id) {
        noteRepository.deleteById(id);
    }
}

