package com.aiqa.platform;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** REST contracts for M51-M60 adaptive UAT intelligence. */
@RestController
@RequestMapping("/api/platform/adaptive-uat")
public class AdaptiveUatIntelligenceController {
    private final AdaptiveUatIntelligenceEngine engine;

    public AdaptiveUatIntelligenceController(AdaptiveUatIntelligenceEngine engine) {
        this.engine = engine;
    }

    @PostMapping("/prioritize")
    public List<AdaptiveUatIntelligenceEngine.PrioritizedTest> prioritize(@RequestBody List<AdaptiveUatIntelligenceEngine.TestCandidate> tests) {
        return engine.prioritize(tests);
    }

    @PostMapping("/traceability")
    public List<AdaptiveUatIntelligenceEngine.TraceabilityResult> traceability(@RequestBody List<AdaptiveUatIntelligenceEngine.RequirementTrace> traces) {
        return engine.traceability(traces);
    }

    @PostMapping("/defect-prediction")
    public AdaptiveUatIntelligenceEngine.DefectPrediction defectPrediction(@RequestBody AdaptiveUatIntelligenceEngine.DefectSignals signals) {
        return engine.predictDefect(signals);
    }

    @PostMapping("/environment-drift")
    public AdaptiveUatIntelligenceEngine.DriftResult environmentDrift(@RequestBody DriftRequest request) {
        return engine.detectDrift(request.expected(), request.actual());
    }

    @PostMapping("/test-data-plan")
    public AdaptiveUatIntelligenceEngine.TestDataPlan testDataPlan(@RequestBody TestDataRequest request) {
        return engine.testDataPlan(request.fieldNames(), request.requestedRows());
    }

    @PostMapping("/coverage-gaps")
    public AdaptiveUatIntelligenceEngine.CoverageGapResult coverageGaps(@RequestBody CoverageRequest request) {
        return engine.coverageGaps(request.requirementIds(), request.coveredRequirementIds());
    }

    @PostMapping("/flaky-diagnosis")
    public AdaptiveUatIntelligenceEngine.FlakyDiagnosis flakyDiagnosis(@RequestBody AdaptiveUatIntelligenceEngine.FlakySignals signals) {
        return engine.diagnoseFlaky(signals);
    }

    @PostMapping("/failure-clusters")
    public List<AdaptiveUatIntelligenceEngine.FailureCluster> failureClusters(@RequestBody List<AdaptiveUatIntelligenceEngine.FailureSample> failures) {
        return engine.clusterFailures(failures);
    }

    @PostMapping("/feedback")
    public AdaptiveUatIntelligenceEngine.FeedbackWeights feedback(@RequestBody FeedbackRequest request) {
        return engine.adaptWeights(request.current(), request.signal());
    }

    @PostMapping("/optimize-mission")
    public AdaptiveUatIntelligenceEngine.MissionOptimization optimizeMission(@RequestBody MissionRequest request) {
        return engine.optimizeMission(request.tests(), request.maxMinutes(), request.requestedAgents());
    }

    public record DriftRequest(Map<String, String> expected, Map<String, String> actual) {}
    public record TestDataRequest(List<String> fieldNames, int requestedRows) {}
    public record CoverageRequest(Set<String> requirementIds, Set<String> coveredRequirementIds) {}
    public record FeedbackRequest(AdaptiveUatIntelligenceEngine.FeedbackWeights current, AdaptiveUatIntelligenceEngine.FeedbackSignal signal) {}
    public record MissionRequest(List<AdaptiveUatIntelligenceEngine.TestCandidate> tests, int maxMinutes, int requestedAgents) {}
}
