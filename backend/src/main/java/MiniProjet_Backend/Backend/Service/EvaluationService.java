package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Evaluation;
import MiniProjet_Backend.Backend.Repository.EvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EvaluationService {
    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private AcademicEvaluationPolicyService academicEvaluationPolicyService;

    public List<Evaluation> getAllEvaluations() {
        return evaluationRepository.findAll().stream()
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .map(this::toPolicyEvaluation)
                .toList();
    }

    public Optional<Evaluation> getEvaluationById(Integer id) {
        return evaluationRepository.findById(id)
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .map(this::toPolicyEvaluation);
    }

    public List<Evaluation> getEvaluationsBySeance(Integer seanceId) {
        return evaluationRepository.findBySeanceId(seanceId).stream()
                .filter(academicEvaluationPolicyService::isAcademicEvaluation)
                .map(this::toPolicyEvaluation)
                .toList();
    }

    public Evaluation saveEvaluation(Evaluation evaluation) {
        return evaluationRepository.save(academicEvaluationPolicyService.applyPolicy(evaluation));
    }

    public Evaluation updateEvaluation(Integer id, Evaluation evaluationDetails) {
        return evaluationRepository.findById(id).map(evaluation -> {
            evaluation.setLibelle(evaluationDetails.getLibelle());
            evaluation.setTypeEvaluation(evaluationDetails.getTypeEvaluation());
            evaluation.setDateEvaluation(evaluationDetails.getDateEvaluation());
            evaluation.setSeance(evaluationDetails.getSeance());
            return evaluationRepository.save(academicEvaluationPolicyService.applyPolicy(evaluation));
        }).orElseThrow(() -> new RuntimeException("Evaluation not found"));
    }

    public void deleteEvaluation(Integer id) {
        evaluationRepository.deleteById(id);
    }

    private Evaluation toPolicyEvaluation(Evaluation evaluation) {
        evaluation.setTypeEvaluation(academicEvaluationPolicyService.normalizeEvaluationType(evaluation.getTypeEvaluation()));
        evaluation.setCoefficient(academicEvaluationPolicyService.effectiveCoefficient(evaluation));
        return evaluation;
    }
}

