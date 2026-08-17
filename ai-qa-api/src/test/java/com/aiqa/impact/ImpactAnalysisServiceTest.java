package com.aiqa.impact;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImpactAnalysisServiceTest {

    private final ImpactAnalysisService service = new ImpactAnalysisService();

    @Test
    void deploymentWorkflowChangeRequiresHighRiskRegression() {
        ImpactAnalysisResponse result = service.analyze(new ImpactAnalysisRequest(
                List.of(".github/workflows/aws.yml", "docker-compose.yml"),
                "deploy workflow changed"));

        assertEquals("HIGH", result.risk());
        assertTrue(result.score() >= 70);
        assertTrue(result.fullRegressionRecommended());
        assertTrue(result.recommendedSuites().contains("deployment-smoke"));
        assertTrue(result.recommendedSuites().contains("full-regression"));
    }

    @Test
    void executionChangeSelectsAgenticUatRegression() {
        ImpactAnalysisResponse result = service.analyze(new ImpactAnalysisRequest(
                List.of("src/main/java/com/aiqa/execution/ExecutionService.java"),
                "browser execution changed"));

        assertEquals("HIGH", result.risk());
        assertTrue(result.recommendedSuites().contains("agentic-uat-regression"));
        assertTrue(result.reasons().stream().anyMatch(reason -> reason.contains("execution")));
    }

    @Test
    void ordinaryDocumentationChangeGetsLowRiskSmokeCoverage() {
        ImpactAnalysisResponse result = service.analyze(new ImpactAnalysisRequest(
                List.of("README.md"),
                "documentation only"));

        assertEquals("LOW", result.risk());
        assertEquals(0, result.score());
        assertFalse(result.fullRegressionRecommended());
        assertTrue(result.recommendedSuites().contains("smoke"));
    }
}
