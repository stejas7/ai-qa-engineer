package com.aiqa.platform;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** M61-M70 dependency, contract, evidence and cross-release intelligence. */
@Service
public class DependencyContractIntelligence {

    public DependencyGraph dependencyGraph(List<DependencyEdge> edges) {
        if (edges == null) edges = List.of();
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        for (DependencyEdge edge : edges) {
            if (edge == null || blank(edge.from()) || blank(edge.to())) continue;
            graph.computeIfAbsent(edge.from(), k -> new LinkedHashSet<>()).add(edge.to());
            graph.computeIfAbsent(edge.to(), k -> new LinkedHashSet<>());
        }
        return new DependencyGraph(graph, graph.size(), edges.size());
    }

    public BlastRadius blastRadius(String changedComponent, List<DependencyEdge> edges) {
        DependencyGraph g = dependencyGraph(edges);
        Set<String> impacted = new LinkedHashSet<>();
        walk(changedComponent, g.graph(), impacted);
        impacted.remove(changedComponent);
        return new BlastRadius(changedComponent, List.copyOf(impacted), impacted.size(), impacted.size() >= 5 ? "HIGH" : impacted.size() >= 2 ? "MEDIUM" : "LOW");
    }

    public ContractAssessment assessContract(ContractSnapshot expected, ContractSnapshot actual) {
        if (expected == null || actual == null) throw new IllegalArgumentException("expected and actual contract snapshots are required");
        List<String> missing = expected.requiredFields().stream().filter(f -> !actual.requiredFields().contains(f)).sorted().toList();
        List<String> newRequired = actual.requiredFields().stream().filter(f -> !expected.requiredFields().contains(f)).sorted().toList();
        boolean versionChanged = !safe(expected.version()).equals(safe(actual.version()));
        boolean breaking = !missing.isEmpty() || !newRequired.isEmpty();
        return new ContractAssessment(breaking, versionChanged, missing, newRequired, breaking ? "BLOCK" : versionChanged ? "REVIEW" : "PASS");
    }

    public DriftAssessment schemaDrift(Map<String, String> expected, Map<String, String> actual) {
        Map<String, String> e = expected == null ? Map.of() : expected;
        Map<String, String> a = actual == null ? Map.of() : actual;
        Set<String> keys = new LinkedHashSet<>(); keys.addAll(e.keySet()); keys.addAll(a.keySet());
        List<Drift> drift = new ArrayList<>();
        for (String key : keys) {
            String left = e.get(key), right = a.get(key);
            if (!safe(left).equals(safe(right))) drift.add(new Drift(key, left == null ? "MISSING" : left, right == null ? "MISSING" : right));
        }
        return new DriftAssessment(drift, drift.isEmpty() ? "STABLE" : drift.size() >= 5 ? "HIGH" : "REVIEW");
    }

    public EnvironmentReadiness environmentReadiness(int healthyDependencies, int totalDependencies, boolean schemaStable, boolean secretsConfigured, boolean testDataReady) {
        int total = Math.max(1, totalDependencies);
        double dependencyScore = Math.min(1.0, Math.max(0, healthyDependencies) / (double) total);
        double score = 100 * (0.45 * dependencyScore + 0.20 * bool(schemaStable) + 0.20 * bool(secretsConfigured) + 0.15 * bool(testDataReady));
        String state = score >= 90 ? "READY" : score >= 70 ? "REVIEW" : "BLOCKED";
        return new EnvironmentReadiness(round(score), state);
    }

    public List<TestCandidate> optimizeSelection(List<TestCandidate> tests, int budgetMinutes) {
        if (tests == null) return List.of();
        int remaining = Math.max(0, budgetMinutes);
        List<TestCandidate> ordered = tests.stream().filter(t -> t != null && t.durationMinutes() > 0)
                .sorted(Comparator.comparingDouble((TestCandidate t) -> -(t.risk() * t.businessCriticality() / t.durationMinutes())))
                .toList();
        List<TestCandidate> selected = new ArrayList<>();
        for (TestCandidate t : ordered) if (t.durationMinutes() <= remaining) { selected.add(t); remaining -= t.durationMinutes(); }
        return selected;
    }

    public EvidenceConfidence evidenceConfidence(int assertions, int passedAssertions, boolean screenshot, boolean trace, boolean video) {
        int total = Math.max(1, assertions);
        double assertionScore = Math.min(1, Math.max(0, passedAssertions) / (double) total);
        double score = 100 * (0.70 * assertionScore + 0.10 * bool(screenshot) + 0.10 * bool(trace) + 0.10 * bool(video));
        return new EvidenceConfidence(round(score), score >= 90 ? "HIGH" : score >= 70 ? "MEDIUM" : "LOW");
    }

    public List<DefectGroup> deduplicateDefects(List<DefectSignal> defects) {
        if (defects == null) return List.of();
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (DefectSignal defect : defects) {
            if (defect == null) continue;
            String key = normalize(defect.component()) + "|" + normalize(defect.signature());
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(defect.id());
        }
        return groups.entrySet().stream().map(e -> new DefectGroup(e.getKey(), List.copyOf(e.getValue()), e.getValue().size())).toList();
    }

    public ReleaseMemory crossReleaseMemory(List<ReleaseOutcome> history) {
        if (history == null || history.isEmpty()) return new ReleaseMemory(0, 0, 0, "NO_HISTORY");
        long blocked = history.stream().filter(r -> r != null && "BLOCKED".equalsIgnoreCase(r.decision())).count();
        double avgRisk = history.stream().filter(r -> r != null).mapToDouble(ReleaseOutcome::risk).average().orElse(0);
        return new ReleaseMemory(history.size(), blocked, round(avgRisk * 100), blocked >= Math.ceil(history.size() * 0.4) ? "TIGHTEN_GATES" : "KEEP_BASELINE");
    }

    private void walk(String node, Map<String, Set<String>> graph, Set<String> visited) {
        if (blank(node) || !visited.add(node)) return;
        for (String next : graph.getOrDefault(node, Set.of())) walk(next, graph, visited);
    }
    private boolean blank(String v){return v == null || v.isBlank();}
    private String safe(String v){return v == null ? "" : v;}
    private String normalize(String v){return safe(v).trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-");}
    private double bool(boolean v){return v ? 1.0 : 0.0;}
    private double round(double v){return Math.round(v * 100.0) / 100.0;}

    public record DependencyEdge(String from, String to){}
    public record DependencyGraph(Map<String, Set<String>> graph, int components, int edges){}
    public record BlastRadius(String changedComponent, List<String> impactedComponents, int impactedCount, String severity){}
    public record ContractSnapshot(String version, Set<String> requiredFields){}
    public record ContractAssessment(boolean breaking, boolean versionChanged, List<String> missingFields, List<String> newRequiredFields, String decision){}
    public record Drift(String key, String expectedType, String actualType){}
    public record DriftAssessment(List<Drift> drift, String severity){}
    public record EnvironmentReadiness(double score, String state){}
    public record TestCandidate(String id, double risk, double businessCriticality, int durationMinutes){}
    public record EvidenceConfidence(double score, String level){}
    public record DefectSignal(String id, String component, String signature){}
    public record DefectGroup(String signature, List<String> defectIds, int count){}
    public record ReleaseOutcome(String releaseId, double risk, String decision){}
    public record ReleaseMemory(int releases, long blockedReleases, double averageRiskPercent, String recommendation){}
}
