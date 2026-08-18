package com.aiqa.healing;

import org.springframework.stereotype.Service;

/**
 * Applies conservative policy to self-healing. Business/assertion failures are never auto-healed.
 * @author Tejas Shah
 */
@Service
public class HealingDecisionService {
    public static final double AUTO_HEAL_THRESHOLD = 0.90;

    private final FailureClassifier classifier;
    private final HealingAttemptRepository attempts;

    public HealingDecisionService(FailureClassifier classifier, HealingAttemptRepository attempts) {
        this.classifier = classifier;
        this.attempts = attempts;
    }

    public HealingDecision evaluate(String testId, String failureMessage, String proposedRepair, double confidence) {
        FailureCategory category = classifier.classify(failureMessage);
        double safeConfidence = Math.max(0.0, Math.min(1.0, confidence));
        boolean allowed = category.isRecoverable() && safeConfidence >= AUTO_HEAL_THRESHOLD;
        String decision = allowed ? "AUTO_HEAL_ALLOWED" : "NO_AUTO_HEAL";
        HealingAttempt attempt = attempts.save(new HealingAttempt(
                testId, category, failureMessage, proposedRepair, safeConfidence, decision));
        return new HealingDecision(attempt.getId(), category, category.isRecoverable(), safeConfidence, decision);
    }

    public record HealingDecision(
            java.util.UUID attemptId,
            FailureCategory category,
            boolean recoverable,
            double confidence,
            String decision) {}
}
