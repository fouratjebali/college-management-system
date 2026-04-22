package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.AttendanceSupervisionDetailDTO;
import MiniProjet_Backend.Backend.DTO.AttendanceSupervisionSessionDTO;
import MiniProjet_Backend.Backend.DTO.AttendanceSupervisionStudentDTO;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Presence;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import MiniProjet_Backend.Backend.Repository.PresenceRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AttendanceSupervisionService {
    private static final String STATUS_OPEN = "OUVERTE";
    private static final String STATUS_CLOSED = "CLOTUREE";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final SeanceRepository seanceRepository;
    private final PresenceRepository presenceRepository;
    private final EtudiantRepository etudiantRepository;

    public AttendanceSupervisionService(
            SeanceRepository seanceRepository,
            PresenceRepository presenceRepository,
            EtudiantRepository etudiantRepository
    ) {
        this.seanceRepository = seanceRepository;
        this.presenceRepository = presenceRepository;
        this.etudiantRepository = etudiantRepository;
    }

    @Transactional(readOnly = true)
    public List<AttendanceSupervisionSessionDTO> getSessions() {
        return seanceRepository.findAll().stream()
                .sorted(Comparator.comparing(Seance::getJoursemaine).thenComparing(Seance::getHeureDebut))
                .map(this::toSessionRow)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttendanceSupervisionDetailDTO getSessionDetails(Integer sessionId) {
        Seance seance = findSession(sessionId);
        Map<Integer, Presence> presencesByStudent = presencesByStudent(seance);
        List<AttendanceSupervisionStudentDTO> students = etudiantRepository.findByGroupeId(seance.getGroupe().getId())
                .stream()
                .sorted(Comparator.comparing(Etudiant::getNomComplet))
                .map(student -> toStudentRow(student, presencesByStudent.get(student.getId())))
                .toList();

        return AttendanceSupervisionDetailDTO.builder()
                .session(toSessionRow(seance))
                .students(students)
                .build();
    }

    @Transactional
    public AttendanceSupervisionDetailDTO closeSession(Integer sessionId) {
        Seance seance = findSession(sessionId);
        seance.setAttendanceStatus(STATUS_CLOSED);
        seance.setAttendanceClosedAt(LocalDateTime.now());
        seanceRepository.save(seance);
        return getSessionDetails(sessionId);
    }

    @Transactional
    public AttendanceSupervisionDetailDTO reopenSession(Integer sessionId) {
        Seance seance = findSession(sessionId);
        seance.setAttendanceStatus(STATUS_OPEN);
        seance.setAttendanceClosedAt(null);
        seanceRepository.save(seance);
        return getSessionDetails(sessionId);
    }

    @Transactional
    public AttendanceSupervisionDetailDTO markCollectiveAbsence(Integer sessionId) {
        Seance seance = findSession(sessionId);
        List<Etudiant> students = etudiantRepository.findByGroupeId(seance.getGroupe().getId());
        LocalDateTime now = LocalDateTime.now();

        students.forEach(student -> {
            Presence presence = presenceRepository.findBySeanceIdAndEtudiantId(seance.getId(), student.getId())
                    .orElseGet(Presence::new);
            presence.setSeance(seance);
            presence.setEtudiant(student);
            presence.setStatut("Absent");
            presence.setDateSaisie(now);
            presenceRepository.save(presence);
        });

        seance.setCollectiveAbsenceStatus("VALIDEE");
        seance.setCollectiveAbsenceConfirmedAt(now);
        if (seance.getCollectiveAbsenceReportedAt() == null) {
            seance.setCollectiveAbsenceReportedAt(now);
        }
        seance.setAttendanceStatus(STATUS_CLOSED);
        seance.setAttendanceClosedAt(now);
        seanceRepository.save(seance);

        return getSessionDetails(sessionId);
    }

    private Seance findSession(Integer sessionId) {
        return seanceRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Seance not found"));
    }

    private AttendanceSupervisionSessionDTO toSessionRow(Seance seance) {
        List<Etudiant> students = etudiantRepository.findByGroupeId(seance.getGroupe().getId());
        List<Presence> presences = presenceRepository.findBySeanceId(seance.getId());
        int expectedCount = students.size();
        int recordedCount = presences.size();
        int absentCount = (int) presences.stream().filter(this::isAbsent).count();
        int lateCount = (int) presences.stream().filter(this::isLate).count();
        int presentCount = (int) presences.stream().filter(this::isPresent).count();
        int missingCount = Math.max(0, expectedCount - recordedCount);
        LocalDateTime lastEntryAt = presences.stream()
                .map(Presence::getDateSaisie)
                .filter(date -> date != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return AttendanceSupervisionSessionDTO.builder()
                .sessionId(seance.getId())
                .subject(seance.getEnseignement().getMatiere().getLibelle())
                .groupId(seance.getGroupe().getId())
                .group(seance.getGroupe().getLibelle())
                .department(seance.getGroupe().getDepartement().getNom())
                .professor(seance.getEnseignement().getProfesseur().getNomComplet())
                .day(seance.getJoursemaine())
                .startTime(seance.getHeureDebut().format(TIME_FORMATTER))
                .endTime(seance.getHeureFin().format(TIME_FORMATTER))
                .room(seance.getBatiment() + " / " + seance.getSalle())
                .type(seance.getTypeSeance())
                .expectedCount(expectedCount)
                .recordedCount(recordedCount)
                .presentCount(presentCount)
                .lateCount(lateCount)
                .absentCount(absentCount)
                .missingCount(missingCount)
                .absenceRate(formatRate(absentCount, expectedCount))
                .status(displayStatus(seance.getAttendanceStatus()))
                .closedAt(formatDate(seance.getAttendanceClosedAt()))
                .lastEntryAt(formatDate(lastEntryAt))
                .collectiveAbsenceStatus(displayCollectiveAbsenceStatus(seance.getCollectiveAbsenceStatus()))
                .collectiveAbsenceReportedAt(formatDate(seance.getCollectiveAbsenceReportedAt()))
                .collectiveAbsenceConfirmedAt(formatDate(seance.getCollectiveAbsenceConfirmedAt()))
                .build();
    }

    private AttendanceSupervisionStudentDTO toStudentRow(Etudiant student, Presence presence) {
        return AttendanceSupervisionStudentDTO.builder()
                .presenceId(presence == null ? null : presence.getId())
                .studentId(student.getId())
                .studentName(student.getNomComplet())
                .matricule(student.getMatricule())
                .status(presence == null ? "Non saisie" : presence.getStatut())
                .date(presence == null ? "" : formatDate(presence.getDateSaisie()))
                .build();
    }

    private Map<Integer, Presence> presencesByStudent(Seance seance) {
        Map<Integer, Presence> presences = new LinkedHashMap<>();
        presenceRepository.findBySeanceId(seance.getId()).forEach(presence ->
                presences.putIfAbsent(presence.getEtudiant().getId(), presence)
        );
        return presences;
    }

    private boolean isAbsent(Presence presence) {
        return normalize(presence).equals("absent");
    }

    private boolean isLate(Presence presence) {
        return normalize(presence).equals("retard");
    }

    private boolean isPresent(Presence presence) {
        String status = normalize(presence);
        return status.equals("present") || status.equals("présent");
    }

    private String normalize(Presence presence) {
        return presence.getStatut() == null ? "" : presence.getStatut().trim().toLowerCase(Locale.ROOT);
    }

    private String displayStatus(String status) {
        return STATUS_CLOSED.equalsIgnoreCase(status) ? "Cloturee" : "Ouverte";
    }

    private String displayCollectiveAbsenceStatus(String status) {
        if ("SIGNALEE".equalsIgnoreCase(status)) {
            return "Signalee";
        }
        if ("VALIDEE".equalsIgnoreCase(status)) {
            return "Validee";
        }
        return "Aucune";
    }

    private String formatRate(int absentCount, int expectedCount) {
        if (expectedCount <= 0) {
            return "--";
        }
        return Math.round((absentCount * 100.0) / expectedCount) + "%";
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(DATE_TIME_FORMATTER);
    }
}
