package com.aiqa.deployment;

/**
 * Result returned by the deterministic deployment failure classifier.
 *
 * @param type classified failure type
 * @param recommendation recommended operational response
 * @param retryRecommended whether an automatic retry is reasonable
 * @author Tejas Shah
 */
public record DeploymentFailureDiagnosis(
        DeploymentFailureType type,
        String recommendation,
        boolean retryRecommended) {
}
