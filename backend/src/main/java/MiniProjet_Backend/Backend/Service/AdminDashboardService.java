package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.AdminDashboardResponseDTO;
import MiniProjet_Backend.Backend.DTO.AdminUserPageResponseDTO;
import MiniProjet_Backend.Backend.Model.Administrateur;
import MiniProjet_Backend.Backend.Model.Departement;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Evaluation;
import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Model.Professeur;
import MiniProjet_Backend.Backend.Model.User;
import MiniProjet_Backend.Backend.Repository.DepartementRepository;
import MiniProjet_Backend.Backend.Repository.EnseignementRepository;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import MiniProjet_Backend.Backend.Repository.EvaluationRepository;
import MiniProjet_Backend.Backend.Repository.GroupeRepository;
import MiniProjet_Backend.Backend.Repository.MatiereRepository;
import MiniProjet_Backend.Backend.Repository.ProfesseurRepository;
import MiniProjet_Backend.Backend.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {
    private static final DateTimeFormatter EXAM_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final ProfesseurRepository professeurRepository;
    private final DepartementRepository departementRepository;
    private final EnseignementRepository enseignementRepository;
    private final MatiereRepository matiereRepository;
    private final GroupeRepository groupeRepository;
    private final EvaluationRepository evaluationRepository;
    private final AcademicEvaluationPolicyService academicEvaluationPolicyService;

    public AdminDashboardService(
            UserRepository userRepository,
            EtudiantRepository etudiantRepository,
            ProfesseurRepository professeurRepository,
            DepartementRepository departementRepository,
            EnseignementRepository enseignementRepository,
            MatiereRepository matiereRepository,
            GroupeRepository groupeRepository,
            EvaluationRepository evaluationRepository,
            AcademicEvaluationPolicyService academicEvaluationPolicyService
    ) {
        this.userRepository = userRepository;
        this.etudiantRepository = etudiantRepository;
        this.professeurRepository = professeurRepository;
        this.departementRepository = departementRepository;
        this.enseignementRepository = enseignementRepository;
        this.matiereRepository = matiereRepository;
        this.groupeRepository = groupeRepository;
        this.evaluationRepository = evaluationRepository;
        this.academicEvaluationPolicyService = academicEvaluationPolicyService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponseDTO getDashboard() {
        return AdminDashboardResponseDTO.builder()
                .stats(buildStats())
                .academicRows(buildAcademicRows())
                .users(List.of())
                .exams(buildExams())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminUserPageResponseDTO getUsersPage(
            int requestedPage,
            int requestedSize,
            String search,
            String role,
            String department,
            String group
    ) {
        int size = Math.min(Math.max(requestedSize, 5), 100);
        List<AdminDashboardResponseDTO.UserRowDTO> users = buildUsers();
        List<AdminDashboardResponseDTO.UserRowDTO> filteredUsers = users.stream()
                .filter(user -> matchesUserFilters(user, search, role, department, group))
                .toList();

        int totalPages = filteredUsers.isEmpty() ? 0 : (int) Math.ceil((double) filteredUsers.size() / size);
        int page = totalPages == 0 ? 0 : Math.min(Math.max(requestedPage, 0), totalPages - 1);
        int start = page * size;
        int end = Math.min(start + size, filteredUsers.size());

        return AdminUserPageResponseDTO.builder()
                .content(filteredUsers.subList(start, end))
                .page(page)
                .size(size)
                .totalElements(filteredUsers.size())
                .totalPages(totalPages)
                .totalUserElements(users.size())
                .activeUserElements(users.stream().filter(user -> "Active".equalsIgnoreCase(user.getStatus())).count())
                .roleCounts(buildRoleCounts(users))
                .departments(buildDepartmentOptions(users))
                .groups(buildGroupOptions())
                .departmentSummaries(buildDepartmentSummaries(users))
                .professorsByDepartment(buildProfessorsByDepartment(users))
                .build();
    }

    private List<AdminDashboardResponseDTO.StatDTO> buildStats() {
        return List.of(
                stat("Etudiants", etudiantRepository.count(), "Comptes etudiants", "light"),
                stat("Professeurs", professeurRepository.count(), "Equipe pedagogique", "steel"),
                stat("Cours actifs", matiereRepository.count(), "Matieres referencees", "warm"),
                stat("Groupes", groupeRepository.count(), "Groupes pedagogiques", "sand")
        );
    }

    private AdminDashboardResponseDTO.StatDTO stat(String label, long value, String trend, String tone) {
        return AdminDashboardResponseDTO.StatDTO.builder()
                .label(label)
                .value(Long.toString(value))
                .trend(trend)
                .tone(tone)
                .build();
    }

    private List<AdminDashboardResponseDTO.AcademicRowDTO> buildAcademicRows() {
        return departementRepository.findAll().stream()
                .sorted(Comparator.comparing(Departement::getNom))
                .map(departement -> {
                    int groupes = groupeRepository.findByDepartementId(departement.getId()).size();
                    int matieres = matiereRepository.findByDepartementId(departement.getId()).size();
                    return AdminDashboardResponseDTO.AcademicRowDTO.builder()
                            .code(buildDepartmentCode(departement.getNom()))
                            .title(departement.getNom())
                            .meta(groupes + " groupes, " + matieres + " matieres")
                            .status("Ouvert")
                            .build();
                })
                .toList();
    }

    private List<AdminDashboardResponseDTO.UserRowDTO> buildUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(user -> AdminDashboardResponseDTO.UserRowDTO.builder()
                        .name(defaultText(user.getNomComplet(), "Utilisateur sans nom"))
                        .email(defaultText(user.getEmail(), "Email non renseigne"))
                        .role(resolveRole(user))
                        .status(user.isActif() ? "Active" : "Pending")
                        .department(defaultText(resolveDepartment(user), "Non affecte"))
                        .group(defaultText(resolveGroup(user), "Non affecte"))
                        .specialty(defaultText(resolveSpecialty(user), "Non renseigne"))
                        .build())
                .toList();
    }

    private boolean matchesUserFilters(
            AdminDashboardResponseDTO.UserRowDTO user,
            String search,
            String role,
            String department,
            String group
    ) {
        String query = normalize(search);
        boolean matchesQuery = query.isBlank()
                || normalize(user.getName()).contains(query)
                || normalize(user.getEmail()).contains(query)
                || normalize(user.getRole()).contains(query)
                || normalize(user.getStatus()).contains(query)
                || normalize(user.getDepartment()).contains(query)
                || normalize(user.getGroup()).contains(query)
                || normalize(user.getSpecialty()).contains(query);

        boolean matchesRole = isAllFilter(role) || equalsFilter(user.getRole(), role);
        boolean matchesDepartment = isAllFilter(department) || equalsFilter(user.getDepartment(), department);
        boolean matchesGroup = isAllFilter(group)
                || equalsFilter(user.getGroup(), group)
                || ("Professeur".equalsIgnoreCase(user.getRole()) && "Tous les groupes".equalsIgnoreCase(group));

        return matchesQuery && matchesRole && matchesDepartment && matchesGroup;
    }

    private Map<String, Long> buildRoleCounts(List<AdminDashboardResponseDTO.UserRowDTO> users) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Etudiant", countRole(users, "Etudiant"));
        counts.put("Professeur", countRole(users, "Professeur"));
        counts.put("Administrateur", countRole(users, "Administrateur"));
        return counts;
    }

    private long countRole(List<AdminDashboardResponseDTO.UserRowDTO> users, String role) {
        return users.stream().filter(user -> role.equalsIgnoreCase(user.getRole())).count();
    }

    private List<String> buildDepartmentOptions(List<AdminDashboardResponseDTO.UserRowDTO> users) {
        Set<String> departments = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        departementRepository.findAll().stream()
                .map(Departement::getNom)
                .filter(this::hasText)
                .forEach(departments::add);
        users.stream()
                .map(AdminDashboardResponseDTO.UserRowDTO::getDepartment)
                .filter(this::hasText)
                .filter(value -> !"Non affecte".equalsIgnoreCase(value))
                .forEach(departments::add);
        return departments.stream().toList();
    }

    private List<String> buildGroupOptions() {
        Set<String> groups = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        groupeRepository.findAll().stream()
                .map(Groupe::getLibelle)
                .filter(this::hasText)
                .forEach(groups::add);
        return groups.stream().toList();
    }

    private List<AdminUserPageResponseDTO.DepartmentUserSummaryDTO> buildDepartmentSummaries(
            List<AdminDashboardResponseDTO.UserRowDTO> users
    ) {
        return buildDepartmentOptions(users).stream()
                .map(department -> AdminUserPageResponseDTO.DepartmentUserSummaryDTO.builder()
                        .department(department)
                        .students(countUsersByRoleAndDepartment(users, "Etudiant", department))
                        .professors(countUsersByRoleAndDepartment(users, "Professeur", department))
                        .build())
                .toList();
    }

    private long countUsersByRoleAndDepartment(
            List<AdminDashboardResponseDTO.UserRowDTO> users,
            String role,
            String department
    ) {
        return users.stream()
                .filter(user -> role.equalsIgnoreCase(user.getRole()))
                .filter(user -> equalsFilter(user.getDepartment(), department))
                .count();
    }

    private List<AdminUserPageResponseDTO.DepartmentProfessorsDTO> buildProfessorsByDepartment(
            List<AdminDashboardResponseDTO.UserRowDTO> users
    ) {
        return users.stream()
                .filter(user -> "Professeur".equalsIgnoreCase(user.getRole()))
                .filter(user -> hasText(user.getDepartment()))
                .collect(Collectors.groupingBy(
                        AdminDashboardResponseDTO.UserRowDTO::getDepartment,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> AdminUserPageResponseDTO.DepartmentProfessorsDTO.builder()
                        .department(entry.getKey())
                        .professors(entry.getValue())
                        .build())
                .toList();
    }

    private boolean isAllFilter(String value) {
        return !hasText(value) || "Tous".equalsIgnoreCase(value.trim());
    }

    private boolean equalsFilter(String actual, String expected) {
        return hasText(actual) && hasText(expected) && actual.trim().equalsIgnoreCase(expected.trim());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isBlank();
    }

    private String defaultText(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private List<AdminDashboardResponseDTO.ExamRowDTO> buildExams() {
        return evaluationRepository.findAll().stream()
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .sorted(Comparator.comparing(Evaluation::getDateEvaluation))
                .map(evaluation -> AdminDashboardResponseDTO.ExamRowDTO.builder()
                        .subject(evaluation.getLibelle())
                        .group(evaluation.getSeance().getGroupe().getLibelle())
                        .date(evaluation.getDateEvaluation().format(EXAM_FORMATTER))
                        .room(evaluation.getSeance().getBatiment() + " / " + evaluation.getSeance().getSalle())
                        .type(academicEvaluationPolicyService.normalizeEvaluationType(evaluation.getTypeEvaluation()))
                        .scope(resolveEvaluationScope(evaluation))
                        .build())
                .toList();
    }

    private String resolveEvaluationScope(Evaluation evaluation) {
        if (evaluation.getSeance() == null || evaluation.getSeance().getGroupe() == null) {
            return "Non affecte";
        }

        Departement departement = evaluation.getSeance().getGroupe().getDepartement();
        return departement == null ? "Non affecte" : departement.getNom();
    }

    private String resolveRole(User user) {
        if (user instanceof Administrateur) {
            return "Administrateur";
        }
        if (user instanceof Professeur) {
            return "Professeur";
        }
        if (user instanceof Etudiant) {
            return "Etudiant";
        }
        return "Utilisateur";
    }

    private String resolveDepartment(User user) {
        if (user instanceof Etudiant etudiant && etudiant.getGroupe() != null) {
            Departement departement = etudiant.getGroupe().getDepartement();
            return departement == null ? "Non affecte" : defaultText(departement.getNom(), "Non affecte");
        }
        if (user instanceof Professeur professeur) {
            return enseignementRepository.findByProfesseurId(professeur.getId()).stream()
                    .findFirst()
                    .map(this::resolveEnseignementDepartment)
                    .orElse("Non affecte");
        }
        if (user instanceof Administrateur) {
            return "Administration";
        }
        return "Non affecte";
    }

    private String resolveEnseignementDepartment(Enseignement enseignement) {
        if (enseignement.getMatiere() == null || enseignement.getMatiere().getDepartement() == null) {
            return "Non affecte";
        }

        return defaultText(enseignement.getMatiere().getDepartement().getNom(), "Non affecte");
    }

    private String resolveGroup(User user) {
        if (user instanceof Etudiant etudiant && etudiant.getGroupe() != null) {
            return defaultText(etudiant.getGroupe().getLibelle(), "Non affecte");
        }
        if (user instanceof Professeur) {
            return "Tous les groupes";
        }
        return "Non affecte";
    }

    private String resolveSpecialty(User user) {
        if (user instanceof Professeur professeur) {
            return defaultText(professeur.getGrade(), "Non renseigne");
        }
        if (user instanceof Etudiant etudiant) {
            return defaultText(etudiant.getNiveau(), "Non renseigne");
        }
        if (user instanceof Administrateur administrateur) {
            return defaultText(administrateur.getFonction(), "Non renseigne");
        }
        return "Profil general";
    }

    private String buildDepartmentCode(String name) {
        String[] words = name == null ? new String[0] : name.trim().split("\\s+");
        StringBuilder code = new StringBuilder();

        for (String word : words) {
            if (!word.isBlank() && code.length() < 3) {
                code.append(Character.toUpperCase(word.charAt(0)));
            }
        }

        return code.isEmpty() ? "DEP" : code.toString();
    }
}
