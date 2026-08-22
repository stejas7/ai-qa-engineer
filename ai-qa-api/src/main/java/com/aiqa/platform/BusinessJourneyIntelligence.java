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

/** M71-M80 business-journey, quality-dimension and requirement intelligence. */
@Service
public class BusinessJourneyIntelligence {

    public JourneyModel journeyModel(List<JourneyStep> steps) {
        if (steps == null) return new JourneyModel(List.of(), 0, "EMPTY");
        List<JourneyStep> ordered = steps.stream().filter(s -> s != null && !blank(s.name()))
                .sorted(Comparator.comparingInt(JourneyStep::order)).toList();
        long critical = ordered.stream().filter(JourneyStep::critical).count();
        return new JourneyModel(ordered, critical, ordered.isEmpty() ? "EMPTY" : "READY");
    }

    public List<JourneyStep> criticalPath(List<JourneyStep> steps, int maxSteps) {
        if (steps == null) return List.of();
        return steps.stream().filter(s -> s != null && s.critical()).sorted(Comparator.comparingInt(JourneyStep::order))
                .limit(Math.max(1, maxSteps)).toList();
    }

    public AccessibilityAssessment accessibility(int totalChecks, int passedChecks, int criticalViolations) {
        int total = Math.max(1, totalChecks);
        double score = 100.0 * Math.max(0, Math.min(total, passedChecks)) / total;
        String state = criticalViolations > 0 ? "BLOCK" : score >= 95 ? "PASS" : score >= 85 ? "REVIEW" : "BLOCK";
        return new AccessibilityAssessment(round(score), Math.max(0, criticalViolations), state);
    }

    public RegressionPlan securityRegression(List<ChangeSignal> changes) {
        return regression(changes, Set.of("auth","security","permission","role","token","secret","session"), "SECURITY");
    }

    public RegressionPlan performanceRegression(List<ChangeSignal> changes) {
        return regression(changes, Set.of("query","cache","loop","batch","api","database","render","latency"), "PERFORMANCE");
    }

    public LocaleCoverage localeCoverage(Set<String> requiredLocales, Set<String> coveredLocales) {
        Set<String> required = normalized(requiredLocales);
        Set<String> covered = normalized(coveredLocales);
        List<String> missing = required.stream().filter(l -> !covered.contains(l)).sorted().toList();
        double ratio = required.isEmpty() ? 100 : 100.0 * (required.size() - missing.size()) / required.size();
        return new LocaleCoverage(round(ratio), missing, missing.isEmpty() ? "PASS" : "REVIEW");
    }

    public List<DeviceTarget> deviceMatrix(Set<String> browsers, Set<String> viewports, int maxTargets) {
        List<DeviceTarget> all = new ArrayList<>();
        for (String browser : normalized(browsers)) for (String viewport : normalized(viewports)) all.add(new DeviceTarget(browser, viewport));
        all.sort(Comparator.comparing(DeviceTarget::browser).thenComparing(DeviceTarget::viewport));
        return all.stream().limit(Math.max(1, maxTargets)).toList();
    }

    public List<Persona> syntheticPersonas(List<String> roles, int maxPersonas) {
        if (roles == null) return List.of();
        return roles.stream().filter(r -> !blank(r)).map(String::trim).distinct().sorted()
                .limit(Math.max(1, maxPersonas)).map(r -> new Persona(r, "synthetic-" + slug(r), List.of("valid", "boundary", "permission"))).toList();
    }

    public List<NegativePath> negativePaths(List<FieldRule> rules, int maxCases) {
        if (rules == null) return List.of();
        List<NegativePath> cases = new ArrayList<>();
        for (FieldRule rule : rules) {
            if (rule == null || blank(rule.field())) continue;
            if (rule.required()) cases.add(new NegativePath(rule.field(), "MISSING_REQUIRED", "omit field"));
            if (rule.maxLength() > 0) cases.add(new NegativePath(rule.field(), "OVER_MAX_LENGTH", "length " + (rule.maxLength() + 1)));
            if (rule.numeric()) cases.add(new NegativePath(rule.field(), "WRONG_TYPE", "non-numeric value"));
        }
        return cases.stream().limit(Math.max(1, maxCases)).toList();
    }

    public AmbiguityAssessment ambiguity(String requirement) {
        String text = requirement == null ? "" : requirement.trim();
        String lower = text.toLowerCase(Locale.ROOT);
        List<String> signals = new ArrayList<>();
        for (String vague : List.of("fast","quickly","user friendly","appropriate","etc","as needed","some","many")) if (lower.contains(vague)) signals.add(vague);
        if (!lower.matches(".*\\b(when|if|given|after|before)\\b.*")) signals.add("missing condition");
        if (!lower.matches(".*\\b(should|must|shall|returns|displays|blocks|allows)\\b.*")) signals.add("missing expected behavior");
        String state = signals.isEmpty() ? "CLEAR" : signals.size() >= 3 ? "AMBIGUOUS" : "REVIEW";
        return new AmbiguityAssessment(state, List.copyOf(new LinkedHashSet<>(signals)), text.length());
    }

    private RegressionPlan regression(List<ChangeSignal> changes, Set<String> keywords, String domain) {
        if (changes == null) return new RegressionPlan(domain, List.of(), "LOW");
        List<String> selected = changes.stream().filter(c -> c != null && matches(c, keywords)).map(ChangeSignal::component).distinct().sorted().toList();
        return new RegressionPlan(domain, selected, selected.size() >= 5 ? "HIGH" : selected.size() >= 2 ? "MEDIUM" : selected.isEmpty() ? "LOW" : "FOCUSED");
    }
    private boolean matches(ChangeSignal c, Set<String> keywords) {
        String haystack = (safe(c.component()) + " " + safe(c.description())).toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(haystack::contains) || c.risk() >= 0.8;
    }
    private Set<String> normalized(Set<String> values) {
        if (values == null) return Set.of();
        Set<String> out = new LinkedHashSet<>();
        values.stream().filter(v -> !blank(v)).map(v -> v.trim().toLowerCase(Locale.ROOT)).sorted().forEach(out::add);
        return out;
    }
    private boolean blank(String v){return v == null || v.isBlank();}
    private String safe(String v){return v == null ? "" : v;}
    private String slug(String v){return v.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)","");}
    private double round(double v){return Math.round(v * 100.0) / 100.0;}

    public record JourneyStep(String name,int order,boolean critical,String expectedOutcome){}
    public record JourneyModel(List<JourneyStep> steps,long criticalSteps,String state){}
    public record AccessibilityAssessment(double score,int criticalViolations,String state){}
    public record ChangeSignal(String component,String description,double risk){}
    public record RegressionPlan(String domain,List<String> components,String severity){}
    public record LocaleCoverage(double coveragePercent,List<String> missingLocales,String state){}
    public record DeviceTarget(String browser,String viewport){}
    public record Persona(String role,String id,List<String> scenarioTypes){}
    public record FieldRule(String field,boolean required,int maxLength,boolean numeric){}
    public record NegativePath(String field,String caseType,String mutation){}
    public record AmbiguityAssessment(String state,List<String> signals,int requirementLength){}
}
