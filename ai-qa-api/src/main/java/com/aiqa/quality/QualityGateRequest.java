package com.aiqa.quality;

public record QualityGateRequest(
        int totalTests,
        int passedTests,
        int failedTests,
        int automatedTests,
        int requirements,
        int coveredRequirements) {
}
