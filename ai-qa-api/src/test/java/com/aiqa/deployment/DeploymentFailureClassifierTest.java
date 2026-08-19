package com.aiqa.deployment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for M8 deployment failure classification.
 *
 * @author Tejas Shah
 */
class DeploymentFailureClassifierTest {

    private final DeploymentFailureClassifier classifier = new DeploymentFailureClassifier();

    @Test
    void classifiesLowDisk() {
        DeploymentFailureDiagnosis result = classifier.classify("ERROR: Less than 3GB free on / after safe cleanup");
        assertEquals(DeploymentFailureType.LOW_DISK, result.type());
        assertTrue(result.retryRecommended());
    }

    @Test
    void classifiesSshFailure() {
        DeploymentFailureDiagnosis result = classifier.classify("ERROR: TCP/22 is not reachable on EC2");
        assertEquals(DeploymentFailureType.SSH_FAILURE, result.type());
        assertTrue(result.retryRecommended());
    }

    @Test
    void classifiesDockerBuildFailureFromArtifactMismatch() {
        DeploymentFailureDiagnosis result = classifier.classify("docker build failed to solve: COPY target/ai-qa-api-2.0.0.jar: no such file or directory");
        assertEquals(DeploymentFailureType.DOCKER_BUILD_FAILURE, result.type());
        assertFalse(result.retryRecommended());
    }

    @Test
    void classifiesDockerPullFailure() {
        DeploymentFailureDiagnosis result = classifier.classify("docker pull ghcr.io/example failed: unauthorized");
        assertEquals(DeploymentFailureType.DOCKER_PULL_FAILURE, result.type());
        assertTrue(result.retryRecommended());
    }

    @Test
    void classifiesDockerStartFailureWithoutBlindRetry() {
        DeploymentFailureDiagnosis result = classifier.classify("ERROR: DOCKER_START_FAILURE - docker compose up failed");
        assertEquals(DeploymentFailureType.DOCKER_START_FAILURE, result.type());
        assertFalse(result.retryRecommended());
    }

    @Test
    void classifiesRollbackFailureWithoutAutomaticRetry() {
        DeploymentFailureDiagnosis result = classifier.classify("M8_ROLLBACK_STATUS=FAILED_RUNTIME_OR_HEALTH");
        assertEquals(DeploymentFailureType.ROLLBACK_FAILURE, result.type());
        assertFalse(result.retryRecommended());
    }

    @Test
    void classifiesMissingPreviousImageAsRollbackFailure() {
        DeploymentFailureDiagnosis result = classifier.classify("M8_ROLLBACK_STATUS=SKIPPED_NO_PREVIOUS_IMAGE");
        assertEquals(DeploymentFailureType.ROLLBACK_FAILURE, result.type());
        assertFalse(result.retryRecommended());
    }

    @Test
    void classifiesApplicationHealthFailure() {
        DeploymentFailureDiagnosis result = classifier.classify("Auravis container failed health verification at /actuator/health");
        assertEquals(DeploymentFailureType.APP_HEALTH_FAILURE, result.type());
        assertFalse(result.retryRecommended());
    }

    @Test
    void classifiesRagRuntimeFailure() {
        DeploymentFailureDiagnosis result = classifier.classify("/api/ai/runtime failed while checking Spring AI RAG vector store");
        assertEquals(DeploymentFailureType.RAG_RUNTIME_FAILURE, result.type());
        assertFalse(result.retryRecommended());
    }

    @Test
    void classifiesPublicEndpointFailure() {
        DeploymentFailureDiagnosis result = classifier.classify("ERROR: Public health endpoint did not report UP");
        assertEquals(DeploymentFailureType.PUBLIC_ENDPOINT_FAILURE, result.type());
        assertTrue(result.retryRecommended());
    }

    @Test
    void fallsBackToUnknown() {
        DeploymentFailureDiagnosis result = classifier.classify("unexpected deployment problem");
        assertEquals(DeploymentFailureType.UNKNOWN, result.type());
        assertFalse(result.retryRecommended());
    }
}
