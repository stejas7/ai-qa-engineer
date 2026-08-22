package com.aiqa.platform;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveUatIntelligenceEngineTest {
    private final AdaptiveUatIntelligenceEngine engine = new AdaptiveUatIntelligenceEngine();

    @Test
    void m51PrioritizesHighestRiskTestFirst() {
        var ranked = engine.prioritize(List.of(
                new AdaptiveUatIntelligenceEngine.TestCandidate("low", 0.2, 0.2, 0.1, 10),
                new AdaptiveUatIntelligenceEngine.TestCandidate("high", 1.0, 0.9, 0.8, 15)));
        assertEquals("high", ranked.getFirst().id());
        assertTrue(ranked.getFirst().score() > ranked.getLast().score());
    }

    @Test
    void m52RequiresTestsAndEvidenceForTraceCoverage() {
        var result = engine.traceability(List.of(
                new AdaptiveUatIntelligenceEngine.RequirementTrace("REQ-1", List.of("T1"), 1),
                new AdaptiveUatIntelligenceEngine.RequirementTrace("REQ-2", List.of(), 0)));
        assertTrue(result.getFirst().covered());
        assertFalse(result.getLast().covered());
    }

    @Test
    void m53PredictsHighDefectRiskFromStrongSignals() {
        var prediction = engine.predictDefect(new AdaptiveUatIntelligenceEngine.DefectSignals(0.9, 0.8, 0.9, 0.8));
        assertEquals("HIGH", prediction.riskBand());
        assertTrue(prediction.focusedUatRecommended());
    }

    @Test
    void m54ReportsOnlyDriftedKeysNotValues() {
        var result = engine.detectDrift(Map.of("java", "21", "region", "ap-south-1"), Map.of("java", "17", "region", "ap-south-1"));
        assertEquals(List.of("java"), result.driftedKeys());
        assertFalse(result.aligned());
    }

    @Test
    void m55DetectsSensitiveFieldsAndBoundsRows() {
        var plan = engine.testDataPlan(List.of("name", "email", "passwordHash"), 50_000);
        assertTrue(plan.maskingRequired());
        assertEquals(10_000, plan.rows());
        assertEquals("SYNTHETIC_ONLY", plan.strategy());
    }

    @Test
    void m56FindsCoverageGaps() {
        var result = engine.coverageGaps(Set.of("R1", "R2", "R3"), Set.of("R1", "R3"));
        assertEquals(List.of("R2"), result.uncoveredRequirements());
        assertFalse(result.complete());
    }

    @Test
    void m57IdentifiesInfrastructureFlakiness() {
        var diagnosis = engine.diagnoseFlaky(new AdaptiveUatIntelligenceEngine.FlakySignals(10, 3, 5));
        assertEquals("INFRASTRUCTURE", diagnosis.probableRootCause());
        assertTrue(diagnosis.quarantineCandidate());
    }

    @Test
    void m58ClustersEquivalentFailureSignatures() {
        var clusters = engine.clusterFailures(List.of(
                new AdaptiveUatIntelligenceEngine.FailureSample("T1", "HTTP 500 order 123 failed"),
                new AdaptiveUatIntelligenceEngine.FailureSample("T2", "HTTP 500 order 999 failed"),
                new AdaptiveUatIntelligenceEngine.FailureSample("T3", "timeout")));
        assertEquals(2, clusters.size());
        assertEquals(2, clusters.getFirst().count());
    }

    @Test
    void m59KeepsFeedbackWeightsNormalized() {
        var weights = engine.adaptWeights(
                new AdaptiveUatIntelligenceEngine.FeedbackWeights(0.35, 0.30, 0.25, 0.10),
                new AdaptiveUatIntelligenceEngine.FeedbackSignal(1, 0, 0, 1));
        double total = weights.businessWeight() + weights.changeWeight() + weights.historyWeight() + weights.durationWeight();
        assertEquals(1.0, total, 0.000001);
        assertTrue(weights.businessWeight() > 0.35);
    }

    @Test
    void m60OptimizesWithinBudgetAndBoundsWorkforce() {
        var result = engine.optimizeMission(List.of(
                new AdaptiveUatIntelligenceEngine.TestCandidate("T1", 1, 1, 1, 20),
                new AdaptiveUatIntelligenceEngine.TestCandidate("T2", 0.7, 0.7, 0.5, 20),
                new AdaptiveUatIntelligenceEngine.TestCandidate("T3", 0.2, 0.2, 0.1, 20)), 40, 50);
        assertEquals(40, result.estimatedMinutes());
        assertEquals(2, result.selectedTestIds().size());
        assertEquals(10, result.workforceSize());
        assertEquals("RISK_OPTIMIZED", result.mode());
    }
}
