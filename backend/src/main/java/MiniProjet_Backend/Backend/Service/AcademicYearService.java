package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.AcademicYearRequestDTO;
import MiniProjet_Backend.Backend.DTO.AcademicYearResponseDTO;
import MiniProjet_Backend.Backend.DTO.SemesterRequestDTO;
import MiniProjet_Backend.Backend.Model.AcademicYear;
import MiniProjet_Backend.Backend.Model.Semester;
import MiniProjet_Backend.Backend.Repository.AcademicYearRepository;
import MiniProjet_Backend.Backend.Repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class AcademicYearService {
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;

    public AcademicYearService(
            AcademicYearRepository academicYearRepository,
            SemesterRepository semesterRepository
    ) {
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
    }

    @Transactional(readOnly = true)
    public List<AcademicYearResponseDTO> getAllAcademicYears() {
        return academicYearRepository.findAll().stream()
                .sorted(Comparator.comparing(AcademicYear::getStartDate).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AcademicYearResponseDTO createAcademicYear(AcademicYearRequestDTO request) {
        validateDateRange(request.getStartDate(), request.getEndDate());
        String label = normalizeText(request.getLabel(), "Academic year label is required");

        if (academicYearRepository.existsByLabel(label)) {
            throw new RuntimeException("Academic year already exists: " + label);
        }

        AcademicYear academicYear = new AcademicYear();
        academicYear.setLabel(label);
        academicYear.setStartDate(request.getStartDate());
        academicYear.setEndDate(request.getEndDate());
        academicYear.setLocked(request.isLocked());
        academicYear.setActive(false);

        AcademicYear saved = academicYearRepository.save(academicYear);

        if (request.isActive()) {
            saved = activateAcademicYearEntity(saved);
        }

        return toResponse(saved);
    }

    @Transactional
    public AcademicYearResponseDTO updateAcademicYear(Integer id, AcademicYearRequestDTO request) {
        validateDateRange(request.getStartDate(), request.getEndDate());
        String label = normalizeText(request.getLabel(), "Academic year label is required");

        AcademicYear academicYear = findAcademicYear(id);
        academicYearRepository.findByLabel(label)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Academic year already exists: " + label);
                });

        academicYear.setLabel(label);
        academicYear.setStartDate(request.getStartDate());
        academicYear.setEndDate(request.getEndDate());
        academicYear.setLocked(request.isLocked());

        AcademicYear saved = academicYearRepository.save(academicYear);

        if (request.isActive()) {
            saved = activateAcademicYearEntity(saved);
        } else if (saved.isActive()) {
            saved.setActive(false);
            saved = academicYearRepository.save(saved);
        }

        return toResponse(saved);
    }

    @Transactional
    public AcademicYearResponseDTO activateAcademicYear(Integer id) {
        return toResponse(activateAcademicYearEntity(findAcademicYear(id)));
    }

    @Transactional
    public AcademicYearResponseDTO createSemester(Integer academicYearId, SemesterRequestDTO request) {
        AcademicYear academicYear = findAcademicYear(academicYearId);
        validateDateRange(request.getStartDate(), request.getEndDate());
        validateSemesterInsideYear(academicYear, request.getStartDate(), request.getEndDate());

        String code = normalizeText(request.getCode(), "Semester code is required").toUpperCase();
        String name = normalizeText(request.getName(), "Semester name is required");

        if (semesterRepository.existsByAcademicYearIdAndCode(academicYearId, code)) {
            throw new RuntimeException("Semester code already exists for this academic year: " + code);
        }

        Semester semester = new Semester();
        semester.setAcademicYear(academicYear);
        semester.setCode(code);
        semester.setName(name);
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setLocked(request.isLocked());
        semester.setActive(false);

        Semester saved = semesterRepository.save(semester);

        if (request.isActive()) {
            saved = activateSemesterEntity(saved);
        }

        return toResponse(saved.getAcademicYear());
    }

    @Transactional
    public AcademicYearResponseDTO updateSemester(Integer semesterId, SemesterRequestDTO request) {
        Semester semester = findSemester(semesterId);
        AcademicYear academicYear = semester.getAcademicYear();

        validateDateRange(request.getStartDate(), request.getEndDate());
        validateSemesterInsideYear(academicYear, request.getStartDate(), request.getEndDate());

        String code = normalizeText(request.getCode(), "Semester code is required").toUpperCase();
        semesterRepository.findByAcademicYearIdOrderByStartDateAsc(academicYear.getId()).stream()
                .filter(existing -> !existing.getId().equals(semesterId))
                .filter(existing -> existing.getCode().equalsIgnoreCase(code))
                .findFirst()
                .ifPresent(existing -> {
                    throw new RuntimeException("Semester code already exists for this academic year: " + code);
                });

        semester.setCode(code);
        semester.setName(normalizeText(request.getName(), "Semester name is required"));
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setLocked(request.isLocked());

        Semester saved = semesterRepository.save(semester);

        if (request.isActive()) {
            saved = activateSemesterEntity(saved);
        } else if (saved.isActive()) {
            saved.setActive(false);
            saved = semesterRepository.save(saved);
        }

        return toResponse(saved.getAcademicYear());
    }

    @Transactional
    public AcademicYearResponseDTO activateSemester(Integer semesterId) {
        Semester semester = activateSemesterEntity(findSemester(semesterId));
        return toResponse(semester.getAcademicYear());
    }

    @Transactional
    public AcademicYearResponseDTO setSemesterLocked(Integer semesterId, boolean locked) {
        Semester semester = findSemester(semesterId);
        semester.setLocked(locked);
        semesterRepository.save(semester);
        return toResponse(semester.getAcademicYear());
    }

    private AcademicYear activateAcademicYearEntity(AcademicYear selectedYear) {
        academicYearRepository.findAll().forEach(year -> {
            year.setActive(year.getId().equals(selectedYear.getId()));
            academicYearRepository.save(year);
        });

        selectedYear.setActive(true);
        return academicYearRepository.save(selectedYear);
    }

    private Semester activateSemesterEntity(Semester selectedSemester) {
        semesterRepository.findAll().forEach(semester -> {
            semester.setActive(semester.getId().equals(selectedSemester.getId()));
            semesterRepository.save(semester);
        });

        academicYearRepository.findAll().forEach(year -> {
            year.setActive(year.getId().equals(selectedSemester.getAcademicYear().getId()));
            academicYearRepository.save(year);
        });

        selectedSemester.setActive(true);
        selectedSemester.getAcademicYear().setActive(true);
        academicYearRepository.save(selectedSemester.getAcademicYear());
        return semesterRepository.save(selectedSemester);
    }

    private AcademicYearResponseDTO toResponse(AcademicYear academicYear) {
        return AcademicYearResponseDTO.builder()
                .id(academicYear.getId())
                .label(academicYear.getLabel())
                .startDate(academicYear.getStartDate())
                .endDate(academicYear.getEndDate())
                .active(academicYear.isActive())
                .locked(academicYear.isLocked())
                .status(resolveStatus(academicYear.isActive(), academicYear.isLocked()))
                .semesters(semesterRepository.findByAcademicYearIdOrderByStartDateAsc(academicYear.getId()).stream()
                        .map(this::toSemesterResponse)
                        .toList())
                .build();
    }

    private AcademicYearResponseDTO.SemesterResponseDTO toSemesterResponse(Semester semester) {
        return AcademicYearResponseDTO.SemesterResponseDTO.builder()
                .id(semester.getId())
                .academicYearId(semester.getAcademicYear().getId())
                .code(semester.getCode())
                .name(semester.getName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .active(semester.isActive())
                .locked(semester.isLocked())
                .status(resolveStatus(semester.isActive(), semester.isLocked()))
                .build();
    }

    private AcademicYear findAcademicYear(Integer id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Academic year not found"));
    }

    private Semester findSemester(Integer id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Semester not found"));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date must be after start date");
        }
    }

    private void validateSemesterInsideYear(AcademicYear academicYear, LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(academicYear.getStartDate()) || endDate.isAfter(academicYear.getEndDate())) {
            throw new RuntimeException("Semester dates must be inside the academic year range");
        }
    }

    private String normalizeText(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(errorMessage);
        }

        return value.trim();
    }

    private String resolveStatus(boolean active, boolean locked) {
        if (active) {
            return locked ? "Actif verrouille" : "Actif";
        }

        return locked ? "Verrouille" : "Planifie";
    }
}
