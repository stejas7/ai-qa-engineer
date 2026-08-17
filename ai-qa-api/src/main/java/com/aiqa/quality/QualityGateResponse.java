package com.aiqa.quality;

public record QualityGateResponse(
        String decision,
        String reason,
        int totalTests,
        int passedTests,
        int failedTests,
        int automatedTests,
        int requirements,
        int coveredRequirements,
        double passRate,
        double automationRate,
        double requirementCoverage) {
}
