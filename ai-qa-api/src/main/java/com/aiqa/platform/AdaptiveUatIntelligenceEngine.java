package com.aiqa.platform;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * M51-M60 adaptive UAT intelligence runtime. The engine is deterministic, bounded and
 * policy-friendly so it can be safely composed with the M39-M50 governance/release gates.
 *
 * @author Tejas Shah
 */
@Service
public class AdaptiveUatIntelligenceEngine {

    /** M51: explainable, deterministic test prioritization. */
    public List<PrioritizedTest> prioritize(List<TestCandidate> tests) {
        if (tests == null) throw new IllegalArgumentException("tests are required");
        return tests.stream().map(test -> {
            double score = clamp(0.35 * test.businessCriticality()
                    + 0.30 * test.changeImpact()
                    + 0.25 * test.failureHistory()
                    + 0.10 * durationUrgency(test.estimatedMinutes()));
            return new PrioritizedTest(test.id(), score, test.estimatedMinutes(),
                    "business+change+history+duration");
        }).sorted(Comparator.comparingDouble(PrioritizedTest::score).reversed()
                .thenComparing(PrioritizedTest::id)).toList();
    }

    /** M52: requirement-to-test/evidence traceability. */
    public List<TraceabilityResult> traceability(List<RequirementTrace> traces) {
        if (traces == null) throw new IllegalArgumentException("traces are required");
        return traces.stream().map(trace -> new TraceabilityResult(
                trace.requirementId(),
                trace.testIds() == null ? 0 : trace.testIds().size(),
                Math.max(0, trace.evidenceCount()),
                trace.testIds() != null && !trace.testIds().isEmpty() && trace.evidenceCount() > 0
        )).toList();
    }

    /** M53: bounded defect-likelihood prediction from normalized engineering signals. */
    public DefectPrediction predictDefect(DefectSignals signals) {
        if (signals == null) throw new IllegalArgumentException("defect signals are required");
        double score = clamp(0.30 * signals.codeChurn()
                + 0.25 * signals.complexity()
                + 0.30 * signals.historicalDefects()
                + 0.15 * signals.changeCoupling());
        String band = score >= 0.75 ? "HIGH" : score >= 0.45 ? "MEDIUM" : "LOW";
        return new DefectPrediction(score, band, score >= 0.45);
    }

    /** M54: environment drift detection without exposing secret values. */
    public DriftResult detectDrift(Map<String, String> expected, Map<String, String> actual) {
        if (expected == null || actual == null) throw new IllegalArgumentException("expected and actual environments are required");
        Set<String> keys = new HashSet<>();
        keys.addAll(expected.keySet());
        keys.addAll(actual.keySet());
        List<String> drifted = keys.stream().filter(key -> !safe(expected.get(key)).equals(safe(actual.get(key))))
                .sorted().toList();
        String severity = drifted.isEmpty() ? "NONE" : drifted.size() <= 2 ? "LOW" : drifted.size() <= 5 ? "MEDIUM" : "HIGH";
        return new DriftResult(drifted, severity, drifted.isEmpty());
    }

    /** M55: privacy-aware synthetic test-data planning. */
    public TestDataPlan testDataPlan(List<String> fieldNames, int requestedRows) {
        if (fieldNames == null) throw new IllegalArgumentException("field names are required");
        int rows = Math.max(1, Math.min(requestedRows, 10_000));
        List<String> sensitive = fieldNames.stream().filter(this::looksSensitive).sorted().toList();
        return new TestDataPlan(rows, fieldNames.size(), sensitive, !sensitive.isEmpty(), "SYNTHETIC_ONLY");
    }

    /** M56: requirement coverage gap analysis. */
    public CoverageGapResult coverageGaps(Set<String> requirementIds, Set<String> coveredRequirementIds) {
        if (requirementIds == null || coveredRequirementIds == null) throw new IllegalArgumentException("coverage sets are required");
        List<String> gaps = requirementIds.stream().filter(id -> !coveredRequirementIds.contains(id)).sorted().toList();
        double coverage = requirementIds.isEmpty() ? 1.0 : (requirementIds.size() - gaps.size()) / (double) requirementIds.size();
        return new CoverageGapResult(coverage, gaps, gaps.isEmpty());
    }

    /** M57: flaky-test diagnosis from outcome transitions and infra-correlated failures. */
    public FlakyDiagnosis diagnoseFlaky(FlakySignals signals) {
        if (signals == null) throw new IllegalArgumentException("flaky signals are required");
        int runs = Math.max(1, signals.totalRuns());
        double transitionRate = clamp(signals.outcomeTransitions() / (double) runs);
        double infraRate = clamp(signals.infrastructureFailures() / (double) runs);
        double likelihood = clamp(0.70 * transitionRate + 0.30 * infraRate);
        String rootCause = infraRate >= 0.4 ? "INFRASTRUCTURE" : transitionRate >= 0.35 ? "NON_DETERMINISTIC_TEST" : "LOW_SIGNAL";
        return new FlakyDiagnosis(likelihood, rootCause, likelihood >= 0.35);
    }

    /** M58: failure clustering by normalized signature. */
    public List<FailureCluster> clusterFailures(List<FailureSample> failures) {
        if (failures == null) throw new IllegalArgumentException("failures are required");
        Map<String, List<String>> clusters = new LinkedHashMap<>();
        failures.forEach(sample -> clusters.computeIfAbsent(normalizeSignature(sample.signature()), ignored -> new ArrayList<>()).add(sample.testId()));
        return clusters.entrySet().stream()
                .map(entry -> new FailureCluster(entry.getKey(), entry.getValue().stream().sorted().toList(), entry.getValue().size()))
                .sorted(Comparator.comparingInt(FailureCluster::count).reversed().thenComparing(FailureCluster::signature))
                .toList();
    }

    /** M59: bounded feedback-weight adaptation; weights always remain normalized and explainable. */
    public FeedbackWeights adaptWeights(FeedbackWeights current, FeedbackSignal signal) {
        if (current == null || signal == null) throw new IllegalArgumentException("current weights and feedback signal are required");
        double business = Math.max(0.05, current.businessWeight() + 0.05 * signal.businessMiss());
        double change = Math.max(0.05, current.changeWeight() + 0.05 * signal.changeMiss());
        double history = Math.max(0.05, current.historyWeight() + 0.05 * signal.historyMiss());
        double duration = Math.max(0.05, current.durationWeight() - 0.03 * signal.durationPenalty());
        double total = business + change + history + duration;
        return new FeedbackWeights(business / total, change / total, history / total, duration / total);
    }

    /** M60: budget-aware mission optimization for bounded multi-agent execution. */
    public MissionOptimization optimizeMission(List<TestCandidate> tests, int maxMinutes, int requestedAgents) {
        if (maxMinutes <= 0) throw new IllegalArgumentException("maxMinutes must be positive");
        List<PrioritizedTest> ranked = prioritize(tests);
        List<String> selected = new ArrayList<>();
        int used = 0;
        for (PrioritizedTest test : ranked) {
            if (used + test.estimatedMinutes() <= maxMinutes) {
                selected.add(test.id());
                used += test.estimatedMinutes();
            }
        }
        int workforceSize = Math.max(1, Math.min(requestedAgents, 10));
        String mode = selected.size() == ranked.size() ? "FULL" : "RISK_OPTIMIZED";
        return new MissionOptimization(selected, used, maxMinutes, workforceSize, mode, ranked.size() - selected.size());
    }

    private double durationUrgency(int minutes) {
        if (minutes <= 0) return 1.0;
        return clamp(1.0 - Math.min(minutes, 120) / 120.0);
    }

    private boolean looksSensitive(String field) {
        String normalized = safe(field).toLowerCase(Locale.ROOT);
        return normalized.contains("password") || normalized.contains("token") || normalized.contains("secret")
                || normalized.contains("ssn") || normalized.contains("aadhaar") || normalized.contains("email")
                || normalized.contains("phone") || normalized.contains("card");
    }

    private String normalizeSignature(String signature) {
        String normalized = safe(signature).toLowerCase(Locale.ROOT).replaceAll("[0-9]+", "#").replaceAll("\\s+", " ").trim();
        return normalized.isBlank() ? "unknown" : normalized;
    }

    private String safe(String value) { return value == null ? "" : value; }
    private double clamp(double value) { return Math.max(0, Math.min(1, value)); }

    public record TestCandidate(String id, double businessCriticality, double changeImpact, double failureHistory, int estimatedMinutes) {}
    public record PrioritizedTest(String id, double score, int estimatedMinutes, String explanation) {}
    public record RequirementTrace(String requirementId, List<String> testIds, int evidenceCount) {}
    public record TraceabilityResult(String requirementId, int linkedTests, int evidenceCount, boolean covered) {}
    public record DefectSignals(double codeChurn, double complexity, double historicalDefects, double changeCoupling) {}
    public record DefectPrediction(double likelihood, String riskBand, boolean focusedUatRecommended) {}
    public record DriftResult(List<String> driftedKeys, String severity, boolean aligned) {}
    public record TestDataPlan(int rows, int fields, List<String> sensitiveFields, boolean maskingRequired, String strategy) {}
    public record CoverageGapResult(double coverageRatio, List<String> uncoveredRequirements, boolean complete) {}
    public record FlakySignals(int totalRuns, int outcomeTransitions, int infrastructureFailures) {}
    public record FlakyDiagnosis(double likelihood, String probableRootCause, boolean quarantineCandidate) {}
    public record FailureSample(String testId, String signature) {}
    public record FailureCluster(String signature, List<String> testIds, int count) {}
    public record FeedbackWeights(double businessWeight, double changeWeight, double historyWeight, double durationWeight) {}
    public record FeedbackSignal(double businessMiss, double changeMiss, double historyMiss, double durationPenalty) {}
    public record MissionOptimization(List<String> selectedTestIds, int estimatedMinutes, int budgetMinutes, int workforceSize, String mode, int deferredTests) {}
}
