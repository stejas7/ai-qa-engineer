package com.aiqa.platform;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnterpriseAutonomyGovernanceTest {
    private final EnterpriseAutonomyGovernance service = new EnterpriseAutonomyGovernance();

    @Test void governanceAndPolicySimulationAreExplainable() {
        var coverage = service.governanceCoverage(10,10,9);
        assertTrue(coverage.score() >= 90);
        var policy = service.simulatePolicy(List.of(
                new EnterpriseAutonomyGovernance.PolicyRule("high-risk",0.7,false,false,"BLOCK"),
                new EnterpriseAutonomyGovernance.PolicyRule("approval",1.0,true,false,"REVIEW")
        ), new EnterpriseAutonomyGovernance.DecisionContext(0.8,true,true));
        assertEquals("BLOCK", policy.decision());
    }

    @Test void approvalIncidentAndRollbackSignalsWork() {
        var approval = service.approvalEfficiency(List.of(
                new EnterpriseAutonomyGovernance.ApprovalEvent("A1",20),
                new EnterpriseAutonomyGovernance.ApprovalEvent("A2",80)
        ),60);
        assertEquals(1, approval.breaches());
        var learning = service.incidentLearning(List.of(
                new EnterpriseAutonomyGovernance.IncidentSignal("I1","database","timeout"),
                new EnterpriseAutonomyGovernance.IncidentSignal("I2","database","pool")
        ));
        assertEquals("FOCUS_DATABASE", learning.recommendation());
        assertEquals("ROLLBACK", service.rollbackRecommendation(0.9,0.8,0.7,true).action());
    }

    @Test void resilienceRetentionAndModelGovernanceAreBounded() {
        assertEquals("RESILIENT", service.resilience(List.of(
                new EnterpriseAutonomyGovernance.ResilienceProbe("db",true),
                new EnterpriseAutonomyGovernance.ResilienceProbe("queue",true)
        )).state());
        var retention = service.retentionPlan(List.of(
                new EnterpriseAutonomyGovernance.DataClass("credentials",true,false),
                new EnterpriseAutonomyGovernance.DataClass("audit",false,true)
        ),180);
        assertEquals(90, retention.rules().stream().filter(r -> r.name().equals("credentials")).findFirst().orElseThrow().days());
        assertEquals(365, retention.rules().stream().filter(r -> r.name().equals("audit")).findFirst().orElseThrow().days());
        assertEquals("APPROVED", service.modelGovernance(0.95,0.05,true,true,true).state());
    }

    @Test void overridesAndReadinessCompleteM100() {
        var overrides = service.overrideAnalytics(List.of(
                new EnterpriseAutonomyGovernance.OverrideEvent("O1","business exception"),
                new EnterpriseAutonomyGovernance.OverrideEvent("O2","business exception")
        ));
        assertEquals(2, overrides.count());
        var readiness = service.autonomyReadiness(
                new EnterpriseAutonomyGovernance.GovernanceCoverage(95,100,90,"STRONG"),
                new EnterpriseAutonomyGovernance.ModelGovernance(94,"APPROVED"),
                new EnterpriseAutonomyGovernance.ResilienceAssessment(96,0,"RESILIENT"),
                true,true,true
        );
        assertEquals("ENTERPRISE_AUTONOMY_READY", readiness.state());
    }
}
