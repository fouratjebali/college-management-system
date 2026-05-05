package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Note;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class NoteWorkflowService {
    public static final String STATUS_DRAFT = "Brouillon";
    public static final String STATUS_SUBMITTED = "Soumise";
    public static final String STATUS_VALIDATED = "Validee";
    public static final String STATUS_REJECTED = "Rejetee";
    public static final String STATUS_PUBLISHED = "Publiee";

    public String normalizeProfessorStatus(String status) {
        String normalized = normalize(status);

        if (normalized.equals("brouillon")) {
            return STATUS_DRAFT;
        }

        return STATUS_SUBMITTED;
    }

    public void markSubmitted(Note note, String requestedStatus) {
        String status = normalizeProfessorStatus(requestedStatus);
        note.setStatut(status);

        if (STATUS_DRAFT.equals(status)) {
            note.setSubmittedAt(null);
        } else if (note.getSubmittedAt() == null) {
            note.setSubmittedAt(LocalDateTime.now());
        }

        note.setValidatedAt(null);
        note.setPublishedAt(null);
        note.setValidationRemark(null);
    }

    public boolean isPublished(Note note) {
        return STATUS_PUBLISHED.equalsIgnoreCase(note.getStatut());
    }

    public boolean isValidatable(Note note) {
        String status = normalize(note.getStatut());
        return status.equals(normalize(STATUS_SUBMITTED)) || status.equals(normalize(STATUS_VALIDATED));
    }

    private String normalize(String status) {
        return status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
    }
}
