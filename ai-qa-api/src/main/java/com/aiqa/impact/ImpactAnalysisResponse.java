package com.aiqa.impact;

import java.util.List;

/** Result of deterministic V12 change-impact analysis. */
public record ImpactAnalysisResponse(
        String risk,
        int score,
        List<String> changedFiles,
        List<String> impactedAreas,
        List<String> recommendedSuites,
        List<String> reasons,
        boolean fullRegressionRecommended) {
}
