package com.aiqa.deployment;

import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Deterministically classifies infrastructure and runtime deployment failures.
 * This intentionally does not require an LLM so CI/CD diagnosis still works
 * when the AI runtime itself is unavailable.
 *
 * @author Tejas Shah
 */
@Service
public class DeploymentFailureClassifier {

    public DeploymentFailureDiagnosis classify(String message) {
        String text = message == null ? "" : message.toLowerCase(Locale.ROOT);

        if (containsAny(text, "less than 3gb", "no space left on device", "disk full", "low disk")) {
            return diagnosis(DeploymentFailureType.LOW_DISK,
                    "Run the EC2 Disk Cleanup workflow, preserve Docker volumes, then redeploy.", true);
        }
        if (containsAny(text, "ssh", "tcp/22", "connection timed out", "connection refused", "permission denied (publickey)")) {
            return diagnosis(DeploymentFailureType.SSH_FAILURE,
                    "Verify EC2 reachability, security-group port 22, host/user secrets and SSH key, then retry.", true);
        }
        if (containsAny(text, "docker build", "failed to solve", "copy failed", "no such file or directory", "target/ai-qa-api-")) {
            return diagnosis(DeploymentFailureType.DOCKER_BUILD_FAILURE,
                    "Verify the Maven artifact path/version used by the Dockerfile before publishing or deploying an image.", false);
        }
        if (containsAny(text, "docker pull", "manifest unknown", "unauthorized", "ghcr", "pull access denied")) {
            return diagnosis(DeploymentFailureType.DOCKER_PULL_FAILURE,
                    "Verify GHCR authentication, image tag/SHA and package permissions before retrying.", true);
        }
        if (containsAny(text, "docker_start_failure", "docker compose up", "container failed to start", "container exited", "container restarting")) {
            return diagnosis(DeploymentFailureType.DOCKER_START_FAILURE,
                    "Inspect docker compose status and ai-qa-api logs, then rollback to the previous image if startup cannot be recovered safely.", false);
        }
        if (containsAny(text, "m8_rollback_status=failed", "m8_rollback_status=skipped_no_previous_image", "rollback did not recover", "rollback failed", "rollback cannot be verified")) {
            return diagnosis(DeploymentFailureType.ROLLBACK_FAILURE,
                    "Stop automatic retries, preserve rollback health/runtime evidence, and inspect the previous image plus container logs before any further deployment change.", false);
        }
        if (containsAny(text, "actuator/health", "container failed health", "status\":\"down", "failed health verification")) {
            return diagnosis(DeploymentFailureType.APP_HEALTH_FAILURE,
                    "Inspect ai-qa-api container logs and rollback to the previous image if health does not recover.", false);
        }
        if (containsAny(text, "/api/ai/runtime", "spring ai", "rag", "vector", "embedding")) {
            return diagnosis(DeploymentFailureType.RAG_RUNTIME_FAILURE,
                    "Verify Spring AI runtime configuration, API credentials and RAG/vector dependencies before promotion.", false);
        }
        if (containsAny(text, "public health endpoint", "duckdns", "502", "503", "504", "public endpoint")) {
            return diagnosis(DeploymentFailureType.PUBLIC_ENDPOINT_FAILURE,
                    "Verify reverse proxy, DNS/TLS and public routing after confirming localhost health is UP.", true);
        }

        return diagnosis(DeploymentFailureType.UNKNOWN,
                "Review the failed GitHub Actions step and attached container logs before deciding whether to retry.", false);
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private DeploymentFailureDiagnosis diagnosis(DeploymentFailureType type, String recommendation,
                                                  boolean retryRecommended) {
        return new DeploymentFailureDiagnosis(type, recommendation, retryRecommended);
    }
}
