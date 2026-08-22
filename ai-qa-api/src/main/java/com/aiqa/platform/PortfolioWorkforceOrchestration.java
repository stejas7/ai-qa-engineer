package com.aiqa.platform;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** M81-M90 portfolio, workforce, capacity, cost and executive release intelligence. */
@Service
public class PortfolioWorkforceOrchestration {

    public ProductPortfolio coordinateProducts(List<ProductMission> missions, int maxConcurrentProducts) {
        if (missions == null) return new ProductPortfolio(List.of(), List.of(), "EMPTY");
        List<ProductMission> ordered = missions.stream().filter(m -> m != null && !blank(m.productId()))
                .sorted(Comparator.comparingDouble(ProductMission::risk).reversed().thenComparing(ProductMission::productId)).toList();
        int cap = Math.max(1, maxConcurrentProducts);
        List<ProductMission> active = ordered.stream().limit(cap).toList();
        List<ProductMission> queued = ordered.stream().skip(cap).toList();
        return new ProductPortfolio(active, queued, queued.isEmpty() ? "READY" : "CAPACITY_QUEUED");
    }

    public ReleaseTrainPlan releaseTrain(List<ReleaseCandidate> releases, int maxParallel) {
        if (releases == null) return new ReleaseTrainPlan(List.of(),0,"EMPTY");
        List<ReleaseCandidate> ordered = releases.stream().filter(r -> r != null)
                .sorted(Comparator.comparing(ReleaseCandidate::targetDate).thenComparingDouble(ReleaseCandidate::risk).reversed()).toList();
        int parallel = Math.max(1,maxParallel);
        List<ReleaseWave> waves = new ArrayList<>();
        for (int i=0;i<ordered.size();i+=parallel) waves.add(new ReleaseWave((i/parallel)+1, ordered.subList(i,Math.min(i+parallel,ordered.size()))));
        return new ReleaseTrainPlan(waves,waves.size(),waves.size()<=2?"COMPACT":"STAGED");
    }

    public CapacityForecast tenantCapacity(int currentConcurrent, int requestedConcurrent, int quota, double growthFactor) {
        int safeQuota = Math.max(1,quota);
        int projected = (int)Math.ceil(Math.max(currentConcurrent,requestedConcurrent) * Math.max(1.0,growthFactor));
        int headroom = safeQuota - projected;
        String state = projected > safeQuota ? "OVER_CAPACITY" : headroom <= Math.ceil(safeQuota * 0.15) ? "TIGHT" : "HEALTHY";
        return new CapacityForecast(projected,safeQuota,headroom,state);
    }

    public CostForecast costForecast(int agentDays, double ratePerAgentDay, double fixedMonthly, double contingencyPercent) {
        int days = Math.max(0,agentDays);
        double variable = days * Math.max(0,ratePerAgentDay);
        double base = variable + Math.max(0,fixedMonthly);
        double contingency = base * Math.max(0,contingencyPercent) / 100.0;
        return new CostForecast(round(variable),round(base),round(contingency),round(base+contingency));
    }

    public WorkforceForecast workforceCapacity(int missions, int specialistsPerMission, int availableAgents, double utilizationTarget) {
        int demand = Math.max(0,missions) * Math.max(1,specialistsPerMission);
        int available = Math.max(0,availableAgents);
        int usable = (int)Math.floor(available * clamp(utilizationTarget));
        int gap = demand - usable;
        return new WorkforceForecast(demand,usable,gap,gap<=0?"SUFFICIENT":"SCALE_REQUIRED");
    }

    public List<AgentAssignment> routeSkills(Set<String> requiredSkills, List<AgentProfile> agents, int maxAgents) {
        if (agents == null || agents.isEmpty()) return List.of();
        Set<String> required = requiredSkills == null ? Set.of() : requiredSkills.stream().filter(s -> !blank(s)).map(this::norm).collect(java.util.stream.Collectors.toSet());
        return agents.stream().filter(a -> a != null).map(a -> {
                    long matches = a.skills()==null?0:a.skills().stream().map(this::norm).filter(required::contains).distinct().count();
                    double coverage = required.isEmpty()?1.0:matches/(double)required.size();
                    double score = 0.7*coverage + 0.3*clamp(a.qualityScore());
                    return new AgentAssignment(a.agentId(),round(score),matches,coverage>=1?"FULL_MATCH":coverage>0?"PARTIAL_MATCH":"NO_MATCH");
                }).sorted(Comparator.comparingDouble(AgentAssignment::score).reversed().thenComparing(AgentAssignment::agentId))
                .limit(Math.max(1,maxAgents)).toList();
    }

    public AgentScorecard scoreAgent(AgentPerformance performance) {
        if (performance == null) throw new IllegalArgumentException("performance is required");
        double success = clamp(performance.successRate());
        double evidence = clamp(performance.evidenceQuality());
        double speed = clamp(performance.slaAdherence());
        double stability = 1.0-clamp(performance.reworkRate());
        double score = 100*(0.35*success+0.30*evidence+0.20*speed+0.15*stability);
        return new AgentScorecard(performance.agentId(),round(score),score>=90?"EXCELLENT":score>=75?"GOOD":score>=60?"REVIEW":"RETRAIN");
    }

    public CalibrationResult calibrate(List<AgentScorecard> scorecards, double targetMean) {
        if (scorecards == null || scorecards.isEmpty()) return new CalibrationResult(0,0,"NO_DATA");
        double mean = scorecards.stream().filter(s -> s != null).mapToDouble(AgentScorecard::score).average().orElse(0);
        double delta = targetMean-mean;
        return new CalibrationResult(round(mean),round(delta),Math.abs(delta)<=5?"CALIBRATED":delta>0?"RAISE_QUALITY_FLOOR":"REDUCE_OVERCONFIDENCE");
    }

    public SlaPlan missionSla(int testCount, int avgMinutesPerTest, int parallelAgents, int evidenceMinutes, int approvalMinutes) {
        int agents = Math.max(1,parallelAgents);
        int execution = (int)Math.ceil(Math.max(0,testCount)*Math.max(1,avgMinutesPerTest)/(double)agents);
        int total = execution + Math.max(0,evidenceMinutes)+Math.max(0,approvalMinutes);
        return new SlaPlan(execution,total,total<=60?"FAST":total<=240?"STANDARD":"EXTENDED");
    }

    public ExecutivePortfolio executivePortfolio(List<PortfolioRelease> releases) {
        if (releases == null || releases.isEmpty()) return new ExecutivePortfolio(0,0,0,0,"NO_RELEASES");
        long ready = releases.stream().filter(r -> r != null && "READY".equalsIgnoreCase(r.decision())).count();
        long blocked = releases.stream().filter(r -> r != null && "BLOCKED".equalsIgnoreCase(r.decision())).count();
        double avgRisk = releases.stream().filter(r -> r != null).mapToDouble(PortfolioRelease::risk).average().orElse(0);
        String state = blocked>0?"ATTENTION":avgRisk>=0.6?"RISK_ELEVATED":"HEALTHY";
        return new ExecutivePortfolio(releases.size(),ready,blocked,round(avgRisk*100),state);
    }

    private boolean blank(String v){return v==null||v.isBlank();}
    private String norm(String v){return v==null?"":v.trim().toLowerCase(Locale.ROOT);}
    private double clamp(double v){return Math.max(0,Math.min(1,v));}
    private double round(double v){return Math.round(v*100.0)/100.0;}

    public record ProductMission(String productId,double risk,int tests){}
    public record ProductPortfolio(List<ProductMission> active,List<ProductMission> queued,String state){}
    public record ReleaseCandidate(String releaseId,String targetDate,double risk){}
    public record ReleaseWave(int wave,List<ReleaseCandidate> releases){}
    public record ReleaseTrainPlan(List<ReleaseWave> waves,int waveCount,String state){}
    public record CapacityForecast(int projectedConcurrent,int quota,int headroom,String state){}
    public record CostForecast(double variableCost,double baseCost,double contingency,double totalForecast){}
    public record WorkforceForecast(int demandAgents,int usableAgents,int gap,String state){}
    public record AgentProfile(String agentId,Set<String> skills,double qualityScore){}
    public record AgentAssignment(String agentId,double score,long skillMatches,String matchState){}
    public record AgentPerformance(String agentId,double successRate,double evidenceQuality,double slaAdherence,double reworkRate){}
    public record AgentScorecard(String agentId,double score,String band){}
    public record CalibrationResult(double currentMean,double adjustment,String recommendation){}
    public record SlaPlan(int executionMinutes,int totalMinutes,String tier){}
    public record PortfolioRelease(String releaseId,double risk,String decision){}
    public record ExecutivePortfolio(int releases,long ready,long blocked,double averageRiskPercent,String state){}
}
