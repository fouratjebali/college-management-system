package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.EliminationRecordDTO;
import MiniProjet_Backend.Backend.DTO.StudentAbsenceSummaryDTO;
import MiniProjet_Backend.Backend.Model.EliminationRecord;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Matiere;
import MiniProjet_Backend.Backend.Model.Presence;
import MiniProjet_Backend.Backend.Repository.EliminationRecordRepository;
import MiniProjet_Backend.Backend.Repository.PresenceRepository;
import MiniProjet_Backend.Backend.Repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AttendanceEliminationService {
    public static final int ELIMINATION_THRESHOLD = 4;
    private static final String STATUS_ELIMINE = "Elimine";
    private static final String STATUS_RENSEIGNE = "Renseigne";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PresenceRepository presenceRepository;
    private final EliminationRecordRepository eliminationRecordRepository;
    private final SemesterRepository semesterRepository;

    public AttendanceEliminationService(
            PresenceRepository presenceRepository,
            EliminationRecordRepository eliminationRecordRepository,
            SemesterRepository semesterRepository
    ) {
        this.presenceRepository = presenceRepository;
        this.eliminationRecordRepository = eliminationRecordRepository;
        this.semesterRepository = semesterRepository;
    }

    @Transactional
    public List<EliminationRecordDTO> getEliminations() {
        syncEliminations();
        return eliminationRecordRepository.findAll().stream()
                .sorted(Comparator.comparing(EliminationRecord::getDetectedAt).reversed())
                .map(this::toRecordRow)
                .toList();
    }

    @Transactional
    public EliminationRecordDTO notifyStudent(Integer id) {
        EliminationRecord record = eliminationRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Elimination record not found"));
        record.setStatus(STATUS_RENSEIGNE);
        record.setNotifiedAt(LocalDateTime.now());
        return toRecordRow(eliminationRecordRepository.save(record));
    }

    @Transactional
    public List<StudentAbsenceSummaryDTO> getStudentAbsenceSummaries(Integer studentId) {
        syncEliminations();
        Map<AbsenceKey, List<Presence>> absences = groupedAbsences().entrySet().stream()
                .filter(entry -> entry.getKey().student().getId().equals(studentId))
                .collect(
                        LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll
                );

        return absences.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().subject().getLibelle()))
                .map(entry -> toStudentAbsenceSummary(entry.getKey(), entry.getValue()))
                .toList();
    }

    private void syncEliminations() {
        groupedAbsences().forEach((key, absences) -> {
            if (absences.size() < ELIMINATION_THRESHOLD) {
                return;
            }

            EliminationRecord record = eliminationRecordRepository
                    .findByEtudiantIdAndMatiereIdAndTypeSeanceIgnoreCase(
                            key.student().getId(),
                            key.subject().getId(),
                            key.type()
                    )
                    .orElseGet(() -> {
                        EliminationRecord newRecord = new EliminationRecord();
                        newRecord.setEtudiant(key.student());
                        newRecord.setMatiere(key.subject());
                        newRecord.setTypeSeance(key.type());
                        newRecord.setStatus(STATUS_ELIMINE);
                        newRecord.setDetectedAt(LocalDateTime.now());
                        return newRecord;
                    });

            record.setAbsenceCount(absences.size());
            eliminationRecordRepository.save(record);
        });
    }

    private Map<AbsenceKey, List<Presence>> groupedAbsences() {
        LocalDate semesterStart = semesterRepository.findByActiveTrue()
                .map(semester -> semester.getStartDate())
                .orElse(LocalDate.MIN);
        LocalDate semesterEnd = semesterRepository.findByActiveTrue()
                .map(semester -> semester.getEndDate())
                .orElse(LocalDate.MAX);

        Map<AbsenceKey, List<Presence>> grouped = new LinkedHashMap<>();
        presenceRepository.findAll().stream()
                .filter(this::isAbsent)
                .filter(presence -> isPedagogicSessionType(presence.getSeance().getTypeSeance()))
                .filter(presence -> {
                    LocalDate date = presence.getDateSaisie().toLocalDate();
                    return !date.isBefore(semesterStart) && !date.isAfter(semesterEnd);
                })
                .forEach(presence -> {
                    AbsenceKey key = new AbsenceKey(
                            presence.getEtudiant(),
                            presence.getSeance().getEnseignement().getMatiere(),
                            normalizeType(presence.getSeance().getTypeSeance())
                    );
                    grouped.computeIfAbsent(key, ignored -> new java.util.ArrayList<>()).add(presence);
                });

        grouped.replaceAll((key, absences) -> absences.stream()
                .sorted(Comparator.comparing(Presence::getDateSaisie).reversed())
                .toList());
        return grouped;
    }

    private StudentAbsenceSummaryDTO toStudentAbsenceSummary(AbsenceKey key, List<Presence> absences) {
        EliminationRecord record = eliminationRecordRepository
                .findByEtudiantIdAndMatiereIdAndTypeSeanceIgnoreCase(
                        key.student().getId(),
                        key.subject().getId(),
                        key.type()
                )
                .orElse(null);

        return StudentAbsenceSummaryDTO.builder()
                .subject(key.subject().getLibelle())
                .professor(absences.get(0).getSeance().getEnseignement().getProfesseur().getNomComplet())
                .typeSeance(key.type())
                .absenceCount(absences.size())
                .threshold(ELIMINATION_THRESHOLD)
                .status(record == null ? "Suivi" : record.getStatus())
                .notifiedAt(record == null ? "" : formatDate(record.getNotifiedAt()))
                .absences(absences.stream().map(this::toAbsenceEntry).toList())
                .build();
    }

    private StudentAbsenceSummaryDTO.StudentAbsenceEntryDTO toAbsenceEntry(Presence presence) {
        return StudentAbsenceSummaryDTO.StudentAbsenceEntryDTO.builder()
                .presenceId(presence.getId())
                .session(presence.getSeance().getJoursemaine() + " "
                        + presence.getSeance().getHeureDebut() + " - "
                        + presence.getSeance().getHeureFin())
                .date(formatDate(presence.getDateSaisie()))
                .room(presence.getSeance().getBatiment() + " / " + presence.getSeance().getSalle())
                .build();
    }

    private EliminationRecordDTO toRecordRow(EliminationRecord record) {
        return EliminationRecordDTO.builder()
                .id(record.getId())
                .studentId(record.getEtudiant().getId())
                .studentName(record.getEtudiant().getNomComplet())
                .matricule(record.getEtudiant().getMatricule())
                .group(record.getEtudiant().getGroupe() == null ? "Non affecte" : record.getEtudiant().getGroupe().getLibelle())
                .subject(record.getMatiere().getLibelle())
                .typeSeance(record.getTypeSeance())
                .absenceCount(record.getAbsenceCount())
                .status(record.getStatus())
                .detectedAt(formatDate(record.getDetectedAt()))
                .notifiedAt(formatDate(record.getNotifiedAt()))
                .build();
    }

    private boolean isAbsent(Presence presence) {
        return presence.getStatut() != null && presence.getStatut().trim().equalsIgnoreCase("Absent");
    }

    private boolean isPedagogicSessionType(String type) {
        String normalizedType = normalizeType(type);
        return normalizedType.equals("Cours") || normalizedType.equals("TD") || normalizedType.equals("TP");
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("td")) {
            return "TD";
        }
        if (normalized.equals("tp")) {
            return "TP";
        }
        if (normalized.equals("cours")) {
            return "Cours";
        }
        return type == null || type.isBlank() ? "Seance" : type.trim();
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(DATE_TIME_FORMATTER);
    }

    private record AbsenceKey(Etudiant student, Matiere subject, String type) {
    }
}
