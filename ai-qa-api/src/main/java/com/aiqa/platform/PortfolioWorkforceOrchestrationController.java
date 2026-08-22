package com.aiqa.platform;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/** REST surface for M81-M90 portfolio and workforce orchestration. */
@RestController
@RequestMapping("/api/platform/portfolio-workforce")
public class PortfolioWorkforceOrchestrationController {
    private final PortfolioWorkforceOrchestration service;

    public PortfolioWorkforceOrchestrationController(PortfolioWorkforceOrchestration service){this.service=service;}

    @PostMapping("/products")
    public PortfolioWorkforceOrchestration.ProductPortfolio products(@RequestBody ProductRequest r){return service.coordinateProducts(r.missions(),r.maxConcurrentProducts());}

    @PostMapping("/release-train")
    public PortfolioWorkforceOrchestration.ReleaseTrainPlan releaseTrain(@RequestBody ReleaseTrainRequest r){return service.releaseTrain(r.releases(),r.maxParallel());}

    @PostMapping("/tenant-capacity")
    public PortfolioWorkforceOrchestration.CapacityForecast tenantCapacity(@RequestBody CapacityRequest r){return service.tenantCapacity(r.currentConcurrent(),r.requestedConcurrent(),r.quota(),r.growthFactor());}

    @PostMapping("/cost-forecast")
    public PortfolioWorkforceOrchestration.CostForecast cost(@RequestBody CostRequest r){return service.costForecast(r.agentDays(),r.ratePerAgentDay(),r.fixedMonthly(),r.contingencyPercent());}

    @PostMapping("/workforce-capacity")
    public PortfolioWorkforceOrchestration.WorkforceForecast workforce(@RequestBody WorkforceRequest r){return service.workforceCapacity(r.missions(),r.specialistsPerMission(),r.availableAgents(),r.utilizationTarget());}

    @PostMapping("/skill-routing")
    public List<PortfolioWorkforceOrchestration.AgentAssignment> skills(@RequestBody SkillRequest r){return service.routeSkills(r.requiredSkills(),r.agents(),r.maxAgents());}

    @PostMapping("/agent-scorecard")
    public PortfolioWorkforceOrchestration.AgentScorecard score(@RequestBody PortfolioWorkforceOrchestration.AgentPerformance p){return service.scoreAgent(p);}

    @PostMapping("/calibration")
    public PortfolioWorkforceOrchestration.CalibrationResult calibrate(@RequestBody CalibrationRequest r){return service.calibrate(r.scorecards(),r.targetMean());}

    @PostMapping("/mission-sla")
    public PortfolioWorkforceOrchestration.SlaPlan sla(@RequestBody SlaRequest r){return service.missionSla(r.testCount(),r.avgMinutesPerTest(),r.parallelAgents(),r.evidenceMinutes(),r.approvalMinutes());}

    @PostMapping("/executive-portfolio")
    public PortfolioWorkforceOrchestration.ExecutivePortfolio portfolio(@RequestBody List<PortfolioWorkforceOrchestration.PortfolioRelease> releases){return service.executivePortfolio(releases);}

    public record ProductRequest(List<PortfolioWorkforceOrchestration.ProductMission> missions,int maxConcurrentProducts){}
    public record ReleaseTrainRequest(List<PortfolioWorkforceOrchestration.ReleaseCandidate> releases,int maxParallel){}
    public record CapacityRequest(int currentConcurrent,int requestedConcurrent,int quota,double growthFactor){}
    public record CostRequest(int agentDays,double ratePerAgentDay,double fixedMonthly,double contingencyPercent){}
    public record WorkforceRequest(int missions,int specialistsPerMission,int availableAgents,double utilizationTarget){}
    public record SkillRequest(Set<String> requiredSkills,List<PortfolioWorkforceOrchestration.AgentProfile> agents,int maxAgents){}
    public record CalibrationRequest(List<PortfolioWorkforceOrchestration.AgentScorecard> scorecards,double targetMean){}
    public record SlaRequest(int testCount,int avgMinutesPerTest,int parallelAgents,int evidenceMinutes,int approvalMinutes){}
}
