package com.aiqa.failure;

public record FailureAnalysisResponse(
    String testId,
    String classification,
    String severity,
    String probableCause,
    String recommendation,
    boolean retryRecommended
) {}
