package com.aiqa.intelligence;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Deterministic M31-M34 intelligence policies. AI may explain these outputs, but Java computes
 * the bounded selection/risk decisions used by product workflows.
 *
 * @author Tejas Shah
 */
@Service
public class AutonomousIntelligenceService {

    public RiskScore risk(RiskRequest request) {
        if (request == null) throw new IllegalArgumentException("risk request is required");
        double score = 0;
        score += clamp(request.businessCriticality(), 0, 5) * 12;
        score += clamp(request.changeComplexity(), 0, 5) * 10;
        score += clamp(request.failureHistory(), 0, 5) * 8;
        score += request.authenticationTouched() ? 12 : 0;
        score += request.paymentOrFinancialFlow() ? 12 : 0;
        score += request.crossSystemChange() ? 8 : 0;
        score = Math.min(100, score);
        String band = score >= 75 ? "CRITICAL" : score >= 50 ? "HIGH" : score >= 25 ? "MEDIUM" : "LOW";
        int recommendedRegressionPercent = band.equals("CRITICAL") ? 100 : band.equals("HIGH") ? 75 : band.equals("MEDIUM") ? 50 : 25;
        return new RiskScore(round(score), band, recommendedRegressionPercent,
                "Deterministic risk score derived from criticality, change complexity, history and sensitive-flow flags.");
    }

    public ChangeImpact impact(ChangeImpactRequest request) {
        if (request == null) throw new IllegalArgumentException("change-impact request is required");
        Set<String> before = tokenize(request.previousRequirement());
        Set<String> after = tokenize(request.currentRequirement());
        Set<String> added = new LinkedHashSet<>(after); added.removeAll(before);
        Set<String> removed = new LinkedHashSet<>(before); removed.removeAll(after);
        double denominator = Math.max(1, Math.max(before.size(), after.size()));
        double changePercent = Math.min(100, ((added.size() + removed.size()) * 100.0) / denominator);
        List<String> signals = new ArrayList<>();
        if (containsAny(after, "login","auth","token","password","oauth")) signals.add("AUTHENTICATION");
        if (containsAny(after, "payment","amount","price","refund","invoice")) signals.add("FINANCIAL_FLOW");
        if (containsAny(after, "api","endpoint","request","response")) signals.add("API_CONTRACT");
        if (containsAny(after, "role","admin","permission","access")) signals.add("AUTHORIZATION");
        if (signals.isEmpty()) signals.add("GENERAL_BEHAVIOR");
        return new ChangeImpact(round(changePercent), added.stream().limit(30).toList(), removed.stream().limit(30).toList(), signals,
                changePercent >= 50 ? "BROAD_REGRESSION" : changePercent >= 20 ? "TARGETED_REGRESSION" : "FOCUSED_SMOKE");
    }

    public FlakyAssessment flaky(FlakyRequest request) {
        if (request == null || request.recentStatuses() == null || request.recentStatuses().isEmpty()) {
            throw new IllegalArgumentException("recentStatuses are required");
        }
        List<String> statuses = request.recentStatuses().stream().map(v -> v == null ? "UNKNOWN" : v.trim().toUpperCase(Locale.ROOT)).toList();
        long pass = statuses.stream().filter("PASS"::equals).count();
        long fail = statuses.stream().filter("FAIL"::equals).count();
        int transitions = 0;
        for (int i = 1; i < statuses.size(); i++) if (!statuses.get(i).equals(statuses.get(i - 1))) transitions++;
        double instability = statuses.size() <= 1 ? 0 : transitions * 100.0 / (statuses.size() - 1);
        boolean flaky = pass > 0 && fail > 0 && instability >= 40;
        return new FlakyAssessment(flaky, round(instability), pass, fail,
                flaky ? "QUARANTINE_AND_RECHECK" : fail > 0 ? "INVESTIGATE_FAILURE" : "STABLE");
    }

    public RegressionPack regression(RegressionPackRequest request) {
        if (request == null || request.candidates() == null) throw new IllegalArgumentException("candidates are required");
        int max = Math.max(1, Math.min(request.maxTests(), 200));
        List<RankedTest> ranked = request.candidates().stream().map(test -> {
            double score = clamp(test.businessRisk(),0,5) * 25
                    + clamp(test.changeImpact(),0,5) * 25
                    + clamp(test.failureHistory(),0,5) * 15
                    + (test.criticalPath() ? 20 : 0)
                    + (test.automated() ? 10 : 0)
                    - (test.flaky() ? 20 : 0);
            return new RankedTest(test.testId(), round(score), test.flaky(), test.automated(), test.criticalPath());
        }).sorted(Comparator.comparingDouble(RankedTest::score).reversed()).limit(max).toList();
        return new RegressionPack(request.candidates().size(), ranked.size(), ranked,
                "Tests are ranked deterministically by risk, impact, history, critical-path value, automation readiness and flakiness penalty.");
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static double round(double value) { return Math.round(value * 100.0) / 100.0; }
    private static Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) return new LinkedHashSet<>();
        return java.util.Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9_]+"))
                .filter(v -> v.length() > 2).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
    private static boolean containsAny(Set<String> tokens, String... values) {
        for (String value : values) if (tokens.contains(value)) return true;
        return false;
    }

    public record RiskRequest(int businessCriticality, int changeComplexity, int failureHistory,
                              boolean authenticationTouched, boolean paymentOrFinancialFlow, boolean crossSystemChange) {}
    public record RiskScore(double score, String band, int recommendedRegressionPercent, String rationale) {}
    public record ChangeImpactRequest(String previousRequirement, String currentRequirement) {}
    public record ChangeImpact(double changePercent, List<String> addedTerms, List<String> removedTerms,
                               List<String> impactedAreas, String regressionStrategy) {}
    public record FlakyRequest(List<String> recentStatuses) {}
    public record FlakyAssessment(boolean flaky, double instabilityPercent, long passed, long failed, String action) {}
    public record TestCandidate(String testId, int businessRisk, int changeImpact, int failureHistory,
                                boolean criticalPath, boolean automated, boolean flaky) {}
    public record RegressionPackRequest(List<TestCandidate> candidates, int maxTests) {}
    public record RankedTest(String testId, double score, boolean flaky, boolean automated, boolean criticalPath) {}
    public record RegressionPack(int totalCandidates, int selectedCount, List<RankedTest> selected, String policy) {}
}
