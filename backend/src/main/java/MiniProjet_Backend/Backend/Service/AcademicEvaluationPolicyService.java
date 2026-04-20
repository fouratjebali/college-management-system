package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Evaluation;
import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Repository.EvaluationRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AcademicEvaluationPolicyService {
    public static final String TYPE_DS = "DS";
    public static final String TYPE_EXAMEN = "Examen";
    public static final String TYPE_EXAMEN_TP = "Examen TP";

    private final EvaluationRepository evaluationRepository;
    private final SeanceRepository seanceRepository;

    public AcademicEvaluationPolicyService(
            EvaluationRepository evaluationRepository,
            SeanceRepository seanceRepository
    ) {
        this.evaluationRepository = evaluationRepository;
        this.seanceRepository = seanceRepository;
    }

    public Evaluation applyPolicy(Evaluation evaluation) {
        String type = normalizeEvaluationType(evaluation.getTypeEvaluation());
        evaluation.setTypeEvaluation(type);
        evaluation.setCoefficient(effectiveCoefficient(type, hasTpComponent(evaluation)));
        return evaluation;
    }

    public boolean isAcademicEvaluation(Evaluation evaluation) {
        try {
            normalizeEvaluationType(evaluation.getTypeEvaluation());
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public String normalizeEvaluationType(String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);

        if (normalized.equals("ds") || normalized.contains("devoir surveille")) {
            return TYPE_DS;
        }
        if (normalized.equals("examen tp") || normalized.equals("exam tp") || normalized.equals("tp")) {
            return TYPE_EXAMEN_TP;
        }
        if (normalized.equals("examen") || normalized.equals("exam")) {
            return TYPE_EXAMEN;
        }

        throw new IllegalArgumentException("Type d'evaluation invalide. Types autorises: DS, Examen, Examen TP");
    }

    public float effectiveCoefficient(Evaluation evaluation) {
        return effectiveCoefficient(normalizeEvaluationType(evaluation.getTypeEvaluation()), hasTpComponent(evaluation));
    }

    public float effectiveCoefficient(String type, boolean hasTpComponent) {
        return switch (normalizeEvaluationType(type)) {
            case TYPE_DS -> 2.0F;
            case TYPE_EXAMEN_TP -> 2.0F;
            case TYPE_EXAMEN -> hasTpComponent ? 6.0F : 8.0F;
            default -> throw new IllegalArgumentException("Type d'evaluation invalide");
        };
    }

    public List<String> requiredTypes(Enseignement enseignement, Groupe groupe) {
        boolean hasTpComponent = hasTpComponent(enseignement, groupe);
        return hasTpComponent
                ? List.of(TYPE_DS, TYPE_EXAMEN_TP, TYPE_EXAMEN)
                : List.of(TYPE_DS, TYPE_EXAMEN);
    }

    public boolean hasTpComponent(Evaluation evaluation) {
        if (TYPE_EXAMEN_TP.equals(normalizeEvaluationType(evaluation.getTypeEvaluation()))) {
            return true;
        }
        if (evaluation.getSeance() == null) {
            return false;
        }
        return hasTpComponent(evaluation.getSeance().getEnseignement(), evaluation.getSeance().getGroupe());
    }

    public boolean hasTpComponent(Enseignement enseignement, Groupe groupe) {
        if (enseignement == null || groupe == null || enseignement.getId() == null || groupe.getId() == null) {
            return false;
        }

        return seanceRepository.findByGroupeId(groupe.getId()).stream()
                .filter(seance -> seance.getEnseignement() != null)
                .filter(seance -> seance.getEnseignement().getId().equals(enseignement.getId()))
                .anyMatch(seance -> isTpSession(seance) || hasExamenTpEvaluation(seance));
    }

    private boolean isTpSession(Seance seance) {
        return seance.getTypeSeance() != null
                && seance.getTypeSeance().trim().equalsIgnoreCase("TP");
    }

    private boolean hasExamenTpEvaluation(Seance seance) {
        if (seance.getId() == null) {
            return false;
        }

        return evaluationRepository.findBySeanceId(seance.getId()).stream()
                .anyMatch(evaluation -> {
                    try {
                        return TYPE_EXAMEN_TP.equals(normalizeEvaluationType(evaluation.getTypeEvaluation()));
                    } catch (IllegalArgumentException exception) {
                        return false;
                    }
                });
    }
}
