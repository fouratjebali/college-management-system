package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.AdminDashboardResponseDTO;
import MiniProjet_Backend.Backend.Model.Administrateur;
import MiniProjet_Backend.Backend.Model.Departement;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Evaluation;
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
import java.util.List;

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
                .users(buildUsers())
                .exams(buildExams())
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
                        .name(user.getNomComplet())
                        .email(user.getEmail())
                        .role(resolveRole(user))
                        .status(user.isActif() ? "Active" : "Pending")
                        .department(resolveDepartment(user))
                        .group(resolveGroup(user))
                        .specialty(resolveSpecialty(user))
                        .build())
                .toList();
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
            return etudiant.getGroupe().getDepartement().getNom();
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

        return enseignement.getMatiere().getDepartement().getNom();
    }

    private String resolveGroup(User user) {
        if (user instanceof Etudiant etudiant && etudiant.getGroupe() != null) {
            return etudiant.getGroupe().getLibelle();
        }
        if (user instanceof Professeur) {
            return "Tous les groupes";
        }
        return "Non affecte";
    }

    private String resolveSpecialty(User user) {
        if (user instanceof Professeur professeur) {
            return professeur.getGrade();
        }
        if (user instanceof Etudiant etudiant) {
            return etudiant.getNiveau();
        }
        if (user instanceof Administrateur administrateur) {
            return administrateur.getFonction();
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
