package com.aiqa.deployment;

/**
 * High-level deployment failure categories used by M8 autonomous UAT operations.
 *
 * @author Tejas Shah
 */
public enum DeploymentFailureType {
    LOW_DISK,
    SSH_FAILURE,
    DOCKER_PULL_FAILURE,
    APP_HEALTH_FAILURE,
    RAG_RUNTIME_FAILURE,
    PUBLIC_ENDPOINT_FAILURE,
    UNKNOWN
}
