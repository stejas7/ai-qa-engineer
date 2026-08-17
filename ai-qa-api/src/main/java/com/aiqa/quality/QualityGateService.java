package com.aiqa.quality;

import org.springframework.stereotype.Service;

/**
 * Produces the deterministic release decision for the AI QA quality gate.
 *
 * <p>The service deliberately evaluates execution facts rather than model-generated opinions.
 * A deployment is approved only when the suite is non-empty, every test passes and all declared
 * requirements are covered.</p>
 */
@Service
public class QualityGateService {

    /**
     * Evaluates UAT execution metrics and returns the release decision with calculated rates.
     *
     * @param request execution and requirement coverage metrics
     * @return calculated quality metrics and an {@code APPROVED} or {@code BLOCKED} decision
     * @throws IllegalArgumentException when the supplied metrics are internally inconsistent
     */
    public QualityGateResponse evaluate(QualityGateRequest request) {
        int total = Math.max(request.totalTests(), 0);
        int passed = Math.max(request.passedTests(), 0);
        int failed = Math.max(request.failedTests(), 0);
        int automated = Math.max(request.automatedTests(), 0);
        int requirements = Math.max(request.requirements(), 0);
        int coveredRequirements = Math.max(request.coveredRequirements(), 0);

        if (passed + failed > total) {
            throw new IllegalArgumentException("passedTests + failedTests cannot exceed totalTests");
        }
        if (automated > total) {
            throw new IllegalArgumentException("automatedTests cannot exceed totalTests");
        }
        if (coveredRequirements > requirements) {
            throw new IllegalArgumentException("coveredRequirements cannot exceed requirements");
        }

        double passRate = total == 0 ? 0.0 : (passed * 100.0) / total;
        double automationRate = total == 0 ? 0.0 : (automated * 100.0) / total;
        double requirementCoverage = requirements == 0
                ? 100.0
                : (coveredRequirements * 100.0) / requirements;

        boolean clean = total > 0
                && failed == 0
                && passed == total
                && requirementCoverage >= 100.0;
        String decision = clean ? "APPROVED" : "BLOCKED";
        String reason = clean
                ? "All tests passed and all requirements are covered"
                : "Quality gate requires attention: failed tests or incomplete requirement coverage";

        return new QualityGateResponse(
                decision,
                reason,
                total,
                passed,
                failed,
                automated,
                requirements,
                coveredRequirements,
                round(passRate),
                round(automationRate),
                round(requirementCoverage));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
