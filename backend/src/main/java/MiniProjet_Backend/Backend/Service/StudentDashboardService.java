package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.StudentDashboardResponseDTO;
import MiniProjet_Backend.Backend.Model.Annonce;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Note;
import MiniProjet_Backend.Backend.Model.Presence;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Model.SupportCours;
import MiniProjet_Backend.Backend.Repository.AnnonceRepository;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import MiniProjet_Backend.Backend.Repository.NoteRepository;
import MiniProjet_Backend.Backend.Repository.PresenceRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import MiniProjet_Backend.Backend.Repository.SupportCoursRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentDashboardService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EtudiantRepository etudiantRepository;
    private final SeanceRepository seanceRepository;
    private final NoteRepository noteRepository;
    private final PresenceRepository presenceRepository;
    private final SupportCoursRepository supportCoursRepository;
    private final AnnonceRepository annonceRepository;

    public StudentDashboardService(
            EtudiantRepository etudiantRepository,
            SeanceRepository seanceRepository,
            NoteRepository noteRepository,
            PresenceRepository presenceRepository,
            SupportCoursRepository supportCoursRepository,
            AnnonceRepository annonceRepository
    ) {
        this.etudiantRepository = etudiantRepository;
        this.seanceRepository = seanceRepository;
        this.noteRepository = noteRepository;
        this.presenceRepository = presenceRepository;
        this.supportCoursRepository = supportCoursRepository;
        this.annonceRepository = annonceRepository;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponseDTO getDashboard(String email) {
        Etudiant student = etudiantRepository.findByEmail(email)
                .orElseGet(() -> etudiantRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Aucun etudiant disponible")));

        List<Seance> schedule = student.getGroupe() == null
                ? List.of()
                : seanceRepository.findByGroupeId(student.getGroupe().getId()).stream()
                .sorted(Comparator.comparing(Seance::getJoursemaine).thenComparing(Seance::getHeureDebut))
                .toList();
        List<Note> grades = noteRepository.findByEtudiantId(student.getId()).stream()
                .sorted(Comparator.comparing(note -> note.getEvaluation().getDateEvaluation()))
                .toList();
        List<Presence> attendance = presenceRepository.findByEtudiantId(student.getId()).stream()
                .sorted(Comparator.comparing(Presence::getDateSaisie).reversed())
                .toList();
        List<SupportCours> materials = resolveMaterials(schedule);
        List<Annonce> announcements = annonceRepository
                .findVisibleForRole("STUDENT", LocalDateTime.now(), true, PageRequest.of(0, 5))
                .getContent();
        List<Seance> makeups = schedule.stream()
                .filter(seance -> seance.getTypeSeance().equalsIgnoreCase("Rattrapage"))
                .toList();

        return StudentDashboardResponseDTO.builder()
                .profile(toProfile(student))
                .stats(buildStats(grades, schedule, materials, announcements, attendance))
                .grades(grades.stream().map(this::toGradeRow).toList())
                .schedule(schedule.stream().map(this::toScheduleRow).toList())
                .materials(materials.stream().map(this::toMaterialRow).toList())
                .announcements(announcements.stream().map(this::toAnnouncementRow).toList())
                .makeups(makeups.stream().map(this::toMakeupRow).toList())
                .attendance(attendance.stream().map(this::toAttendanceRow).toList())
                .build();
    }

    private StudentDashboardResponseDTO.ProfileDTO toProfile(Etudiant student) {
        return StudentDashboardResponseDTO.ProfileDTO.builder()
                .id(student.getId())
                .name(student.getNomComplet())
                .email(student.getEmail())
                .matricule(student.getMatricule())
                .level(student.getNiveau())
                .group(student.getGroupe() == null ? "Non affecte" : student.getGroupe().getLibelle())
                .department(student.getGroupe() == null ? "Non affecte" : student.getGroupe().getDepartement().getNom())
                .year(student.getGroupe() == null ? "Non renseigne" : student.getGroupe().getAnneeUniversitaire())
                .build();
    }

    private List<StudentDashboardResponseDTO.StatDTO> buildStats(
            List<Note> grades,
            List<Seance> schedule,
            List<SupportCours> materials,
            List<Annonce> announcements,
            List<Presence> attendance
    ) {
        return List.of(
                stat("Moyenne", calculateAverage(grades), grades.size() + " note(s)", "light"),
                stat("Seances", Integer.toString(schedule.size()), "Emploi du temps", "steel"),
                stat("Supports", Integer.toString(materials.size()), "Documents disponibles", "warm"),
                stat("Presence", calculateAttendanceRate(attendance), "Taux actuel", "sand"),
                stat("Annonces", Integer.toString(announcements.size()), "Messages actifs", "steel")
        );
    }

    private StudentDashboardResponseDTO.StatDTO stat(
            String label,
            String value,
            String trend,
            String tone
    ) {
        return StudentDashboardResponseDTO.StatDTO.builder()
                .label(label)
                .value(value)
                .trend(trend)
                .tone(tone)
                .build();
    }

    private String calculateAverage(List<Note> grades) {
        if (grades.isEmpty()) {
            return "--";
        }

        double average = grades.stream()
                .mapToDouble(Note::getValeur)
                .average()
                .orElse(0);
        return String.format("%.2f", average);
    }

    private String calculateAttendanceRate(List<Presence> attendance) {
        if (attendance.isEmpty()) {
            return "--";
        }

        long presentCount = attendance.stream()
                .filter(presence -> !presence.getStatut().equalsIgnoreCase("Absent"))
                .count();
        long rate = Math.round((presentCount * 100.0) / attendance.size());
        return rate + "%";
    }

    private List<SupportCours> resolveMaterials(List<Seance> schedule) {
        Map<Integer, Enseignement> teachings = new LinkedHashMap<>();
        schedule.forEach(seance -> teachings.putIfAbsent(
                seance.getEnseignement().getId(),
                seance.getEnseignement()
        ));

        return teachings.values().stream()
                .flatMap(enseignement -> supportCoursRepository.findByEnseignementId(enseignement.getId()).stream())
                .sorted(Comparator.comparing(SupportCours::getDateDepot).reversed())
                .toList();
    }

    private StudentDashboardResponseDTO.GradeRowDTO toGradeRow(Note note) {
        Seance seance = note.getEvaluation().getSeance();

        return StudentDashboardResponseDTO.GradeRowDTO.builder()
                .id(note.getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .evaluation(note.getEvaluation().getLibelle())
                .type(note.getEvaluation().getTypeEvaluation())
                .date(note.getEvaluation().getDateEvaluation().format(DATE_TIME_FORMATTER))
                .value(Float.toString(note.getValeur()))
                .coefficient(Float.toString(note.getEvaluation().getCoefficient()))
                .status(note.getStatut())
                .remark(note.getRemarque())
                .build();
    }

    private StudentDashboardResponseDTO.ScheduleRowDTO toScheduleRow(Seance seance) {
        return StudentDashboardResponseDTO.ScheduleRowDTO.builder()
                .id(seance.getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .professor(seance.getEnseignement().getProfesseur().getNomComplet())
                .day(seance.getJoursemaine())
                .start(seance.getHeureDebut().toString())
                .end(seance.getHeureFin().toString())
                .room(seance.getBatiment() + " / " + seance.getSalle())
                .type(seance.getTypeSeance())
                .build();
    }

    private StudentDashboardResponseDTO.MaterialRowDTO toMaterialRow(SupportCours supportCours) {
        return StudentDashboardResponseDTO.MaterialRowDTO.builder()
                .id(supportCours.getId())
                .title(supportCours.getTitre())
                .subject(supportCours.getEnseignement().getMatiere().getLibelle())
                .fileName(supportCours.getNomFichierOriginal())
                .fileType(supportCours.getTypeFichier())
                .size(formatBytes(supportCours.getTailleOctets()))
                .date(supportCours.getDateDepot().format(DATE_TIME_FORMATTER))
                .downloadUrl("/api/supports/" + supportCours.getId() + "/download")
                .build();
    }

    private StudentDashboardResponseDTO.AnnouncementRowDTO toAnnouncementRow(Annonce annonce) {
        return StudentDashboardResponseDTO.AnnouncementRowDTO.builder()
                .id(annonce.getId())
                .title(annonce.getTitre())
                .content(annonce.getContenu())
                .publicationDate(annonce.getDatePublication().format(DATE_TIME_FORMATTER))
                .expirationDate(annonce.getDateExpiration().format(DATE_TIME_FORMATTER))
                .author(annonce.getAdministrateur() == null ? "Administration" : annonce.getAdministrateur().getNomComplet())
                .build();
    }

    private StudentDashboardResponseDTO.MakeupRowDTO toMakeupRow(Seance seance) {
        return StudentDashboardResponseDTO.MakeupRowDTO.builder()
                .id(seance.getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .professor(seance.getEnseignement().getProfesseur().getNomComplet())
                .day(seance.getJoursemaine())
                .start(seance.getHeureDebut().toString())
                .end(seance.getHeureFin().toString())
                .room(seance.getBatiment() + " / " + seance.getSalle())
                .build();
    }

    private StudentDashboardResponseDTO.AttendanceRowDTO toAttendanceRow(Presence presence) {
        Seance seance = presence.getSeance();

        return StudentDashboardResponseDTO.AttendanceRowDTO.builder()
                .id(presence.getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .session(seance.getJoursemaine() + " " + seance.getHeureDebut() + " - " + seance.getHeureFin())
                .status(presence.getStatut())
                .date(presence.getDateSaisie().format(DATE_TIME_FORMATTER))
                .build();
    }

    private String formatBytes(Long size) {
        if (size == null || size <= 0) {
            return "0 Ko";
        }

        if (size < 1024 * 1024) {
            return Math.max(1, size / 1024) + " Ko";
        }

        return String.format("%.1f Mo", size / (1024.0 * 1024.0));
    }
}
