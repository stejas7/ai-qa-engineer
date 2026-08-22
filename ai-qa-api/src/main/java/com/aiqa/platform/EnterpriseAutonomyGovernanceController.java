package com.aiqa.platform;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST surface for M91-M100 enterprise autonomy governance. */
@RestController
@RequestMapping("/api/platform/autonomy-governance")
public class EnterpriseAutonomyGovernanceController {
    private final EnterpriseAutonomyGovernance service;

    public EnterpriseAutonomyGovernanceController(EnterpriseAutonomyGovernance service){this.service=service;}

    @PostMapping("/governance-coverage")
    public EnterpriseAutonomyGovernance.GovernanceCoverage governance(@RequestBody GovernanceRequest r){return service.governanceCoverage(r.requiredControls(),r.implementedControls(),r.evidencedControls());}

    @PostMapping("/policy-simulation")
    public EnterpriseAutonomyGovernance.PolicySimulation policy(@RequestBody PolicyRequest r){return service.simulatePolicy(r.rules(),r.context());}

    @PostMapping("/approval-efficiency")
    public EnterpriseAutonomyGovernance.ApprovalEfficiency approval(@RequestBody ApprovalRequest r){return service.approvalEfficiency(r.events(),r.targetMinutes());}

    @PostMapping("/incident-learning")
    public EnterpriseAutonomyGovernance.IncidentLearning incidents(@RequestBody List<EnterpriseAutonomyGovernance.IncidentSignal> incidents){return service.incidentLearning(incidents);}

    @PostMapping("/rollback")
    public EnterpriseAutonomyGovernance.RollbackRecommendation rollback(@RequestBody RollbackRequest r){return service.rollbackRecommendation(r.releaseRisk(),r.errorRate(),r.latencyRegression(),r.criticalIncident());}

    @PostMapping("/resilience")
    public EnterpriseAutonomyGovernance.ResilienceAssessment resilience(@RequestBody List<EnterpriseAutonomyGovernance.ResilienceProbe> probes){return service.resilience(probes);}

    @PostMapping("/retention")
    public EnterpriseAutonomyGovernance.RetentionPlan retention(@RequestBody RetentionRequest r){return service.retentionPlan(r.data(),r.defaultDays());}

    @PostMapping("/model-governance")
    public EnterpriseAutonomyGovernance.ModelGovernance model(@RequestBody ModelRequest r){return service.modelGovernance(r.evalScore(),r.driftScore(),r.humanOverrideAvailable(),r.promptVersioned(),r.evidenceStored());}

    @PostMapping("/override-analytics")
    public EnterpriseAutonomyGovernance.OverrideAnalytics overrides(@RequestBody List<EnterpriseAutonomyGovernance.OverrideEvent> overrides){return service.overrideAnalytics(overrides);}

    @PostMapping("/readiness")
    public EnterpriseAutonomyGovernance.AutonomyReadiness readiness(@RequestBody ReadinessRequest r){return service.autonomyReadiness(r.governance(),r.model(),r.resilience(),r.auditComplete(),r.humanOverride(),r.rollbackReady());}

    public record GovernanceRequest(int requiredControls,int implementedControls,int evidencedControls){}
    public record PolicyRequest(List<EnterpriseAutonomyGovernance.PolicyRule> rules,EnterpriseAutonomyGovernance.DecisionContext context){}
    public record ApprovalRequest(List<EnterpriseAutonomyGovernance.ApprovalEvent> events,long targetMinutes){}
    public record RollbackRequest(double releaseRisk,double errorRate,double latencyRegression,boolean criticalIncident){}
    public record RetentionRequest(List<EnterpriseAutonomyGovernance.DataClass> data,int defaultDays){}
    public record ModelRequest(double evalScore,double driftScore,boolean humanOverrideAvailable,boolean promptVersioned,boolean evidenceStored){}
    public record ReadinessRequest(EnterpriseAutonomyGovernance.GovernanceCoverage governance,EnterpriseAutonomyGovernance.ModelGovernance model,EnterpriseAutonomyGovernance.ResilienceAssessment resilience,boolean auditComplete,boolean humanOverride,boolean rollbackReady){}
}
