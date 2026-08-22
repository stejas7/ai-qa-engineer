package com.aiqa.platform;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** M91-M100 enterprise autonomy, governance, resilience and operating-model intelligence. */
@Service
public class EnterpriseAutonomyGovernance {

    public GovernanceCoverage governanceCoverage(int requiredControls, int implementedControls, int evidencedControls) {
        int required = Math.max(1, requiredControls);
        double implementation = Math.min(1.0, Math.max(0, implementedControls) / (double) required);
        double evidence = Math.min(1.0, Math.max(0, evidencedControls) / (double) required);
        double score = 100 * (0.55 * implementation + 0.45 * evidence);
        return new GovernanceCoverage(round(score), round(implementation * 100), round(evidence * 100), score >= 90 ? "STRONG" : score >= 75 ? "REVIEW" : "GAP");
    }

    public PolicySimulation simulatePolicy(List<PolicyRule> rules, DecisionContext context) {
        if (context == null) throw new IllegalArgumentException("decision context is required");
        List<PolicyOutcome> outcomes = new ArrayList<>();
        if (rules != null) {
            for (PolicyRule rule : rules) {
                if (rule == null || blank(rule.name())) continue;
                boolean triggered = context.risk() >= rule.minRisk() || (rule.requireApproval() && !context.approved()) || (rule.requireHealthySlo() && !context.sloHealthy());
                outcomes.add(new PolicyOutcome(rule.name(), triggered, triggered ? rule.action() : "NO_ACTION"));
            }
        }
        boolean blocked = outcomes.stream().anyMatch(o -> o.triggered() && "BLOCK".equalsIgnoreCase(o.action()));
        boolean review = outcomes.stream().anyMatch(o -> o.triggered() && "REVIEW".equalsIgnoreCase(o.action()));
        return new PolicySimulation(outcomes, blocked ? "BLOCK" : review ? "REVIEW" : "PASS");
    }

    public ApprovalEfficiency approvalEfficiency(List<ApprovalEvent> events, long targetMinutes) {
        if (events == null || events.isEmpty()) return new ApprovalEfficiency(0,0,"NO_DATA");
        double avg = events.stream().filter(e -> e != null).mapToLong(ApprovalEvent::durationMinutes).average().orElse(0);
        long breaches = events.stream().filter(e -> e != null && e.durationMinutes() > targetMinutes).count();
        return new ApprovalEfficiency(round(avg), breaches, breaches == 0 ? "ON_TARGET" : breaches < Math.max(2, events.size()/3) ? "REVIEW" : "SLOW");
    }

    public IncidentLearning incidentLearning(List<IncidentSignal> incidents) {
        if (incidents == null || incidents.isEmpty()) return new IncidentLearning(Map.of(), "NO_HISTORY");
        Map<String,Integer> counts = new LinkedHashMap<>();
        for (IncidentSignal incident : incidents) {
            if (incident == null) continue;
            String key = normalize(incident.category());
            counts.merge(key, 1, Integer::sum);
        }
        String top = counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("none");
        return new IncidentLearning(counts, "FOCUS_" + top.toUpperCase(Locale.ROOT));
    }

    public RollbackRecommendation rollbackRecommendation(double releaseRisk, double errorRate, double latencyRegression, boolean criticalIncident) {
        double score = 0.45 * clamp(releaseRisk) + 0.25 * clamp(errorRate) + 0.20 * clamp(latencyRegression) + 0.10 * (criticalIncident ? 1 : 0);
        String action = criticalIncident || score >= 0.70 ? "ROLLBACK" : score >= 0.45 ? "HOLD_AND_REVIEW" : "CONTINUE";
        return new RollbackRecommendation(round(score * 100), action);
    }

    public ResilienceAssessment resilience(List<ResilienceProbe> probes) {
        if (probes == null || probes.isEmpty()) return new ResilienceAssessment(0,0,"NO_DATA");
        long passed = probes.stream().filter(p -> p != null && p.passed()).count();
        long total = probes.stream().filter(p -> p != null).count();
        double score = total == 0 ? 0 : 100.0 * passed / total;
        return new ResilienceAssessment(round(score), total-passed, score >= 95 ? "RESILIENT" : score >= 80 ? "REVIEW" : "FRAGILE");
    }

    public RetentionPlan retentionPlan(List<DataClass> data, int defaultDays) {
        if (data == null) return new RetentionPlan(List.of(), Math.max(1,defaultDays));
        List<RetentionRule> rules = data.stream().filter(d -> d != null && !blank(d.name())).map(d -> {
            int days = d.sensitive() ? Math.min(Math.max(1,defaultDays),90) : Math.max(1,defaultDays);
            if (d.auditCritical()) days = Math.max(days,365);
            return new RetentionRule(d.name(),days,d.sensitive(),d.auditCritical());
        }).sorted(Comparator.comparing(RetentionRule::name)).toList();
        return new RetentionPlan(rules, Math.max(1,defaultDays));
    }

    public ModelGovernance modelGovernance(double evalScore, double driftScore, boolean humanOverrideAvailable, boolean promptVersioned, boolean evidenceStored) {
        double score = 100 * (0.35*clamp(evalScore) + 0.20*(1-clamp(driftScore)) + 0.15*bool(humanOverrideAvailable) + 0.15*bool(promptVersioned) + 0.15*bool(evidenceStored));
        return new ModelGovernance(round(score), score >= 90 ? "APPROVED" : score >= 75 ? "REVIEW" : "RESTRICT");
    }

    public OverrideAnalytics overrideAnalytics(List<OverrideEvent> overrides) {
        if (overrides == null || overrides.isEmpty()) return new OverrideAnalytics(0, Map.of(), "NO_OVERRIDES");
        Map<String,Integer> reasons = new LinkedHashMap<>();
        for (OverrideEvent o : overrides) if (o != null) reasons.merge(normalize(o.reason()),1,Integer::sum);
        String recommendation = overrides.size() >= 10 ? "REVIEW_POLICY_THRESHOLDS" : "MONITOR";
        return new OverrideAnalytics(overrides.size(), reasons, recommendation);
    }

    public AutonomyReadiness autonomyReadiness(GovernanceCoverage governance, ModelGovernance model, ResilienceAssessment resilience, boolean auditComplete, boolean humanOverride, boolean rollbackReady) {
        if (governance == null || model == null || resilience == null) throw new IllegalArgumentException("governance, model and resilience are required");
        double score = 0.25*governance.score() + 0.25*model.score() + 0.20*resilience.score() + 10*bool(auditComplete) + 10*bool(humanOverride) + 10*bool(rollbackReady);
        String state = score >= 90 ? "ENTERPRISE_AUTONOMY_READY" : score >= 75 ? "CONTROLLED_AUTONOMY" : "HUMAN_LED";
        return new AutonomyReadiness(round(score), state);
    }

    private boolean blank(String v){return v==null||v.isBlank();}
    private String normalize(String v){return v==null?"unknown":v.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-");}
    private double clamp(double v){return Math.max(0,Math.min(1,v));}
    private double bool(boolean v){return v?1.0:0.0;}
    private double round(double v){return Math.round(v*100.0)/100.0;}

    public record GovernanceCoverage(double score,double implementationPercent,double evidencePercent,String state){}
    public record PolicyRule(String name,double minRisk,boolean requireApproval,boolean requireHealthySlo,String action){}
    public record DecisionContext(double risk,boolean approved,boolean sloHealthy){}
    public record PolicyOutcome(String rule,boolean triggered,String action){}
    public record PolicySimulation(List<PolicyOutcome> outcomes,String decision){}
    public record ApprovalEvent(String id,long durationMinutes){}
    public record ApprovalEfficiency(double averageMinutes,long breaches,String state){}
    public record IncidentSignal(String id,String category,String summary){}
    public record IncidentLearning(Map<String,Integer> categoryCounts,String recommendation){}
    public record RollbackRecommendation(double score,String action){}
    public record ResilienceProbe(String name,boolean passed){}
    public record ResilienceAssessment(double score,long failedProbes,String state){}
    public record DataClass(String name,boolean sensitive,boolean auditCritical){}
    public record RetentionRule(String name,int days,boolean sensitive,boolean auditCritical){}
    public record RetentionPlan(List<RetentionRule> rules,int defaultDays){}
    public record ModelGovernance(double score,String state){}
    public record OverrideEvent(String id,String reason){}
    public record OverrideAnalytics(int count,Map<String,Integer> reasons,String recommendation){}
    public record AutonomyReadiness(double score,String state){}
}
