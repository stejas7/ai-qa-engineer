package com.aiqa.platform;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlatformRuntimeEnginesTest {
    @Test void governanceRequiresApprovalForHighRiskProductionChange(){
        var g=new GovernanceEngine();
        var d=g.evaluate(new GovernanceEngine.PolicyInput("tenant-a","qa@tenant",0.72,true,true,false));
        assertTrue(d.allowed());
        assertTrue(d.approvalRequired());
        assertFalse(g.evidence("tenant-a").isEmpty());
    }

    @Test void reliabilityAppliesBackpressureAndSloDegradation(){
        var r=new ReliabilityEngine();
        assertTrue(r.acquire("tenant-a",1).allowed());
        assertFalse(r.acquire("tenant-a",1).allowed());
        r.observe("tenant-a",5000,true);
        var slo=r.slo("tenant-a",0.05,2500);
        assertFalse(slo.healthy());
        assertEquals("DEGRADE_AUTONOMY",slo.policy());
    }

    @Test void autonomousGateOnlyPassesWhenAllIndependentGatesPass(){
        var a=new AutonomousReleaseEngine();
        var release=new AutonomousReleaseEngine.ReleaseInput(.1,.0,.0,.0,.1);
        var votes=List.of(
                new AutonomousReleaseEngine.AgentVote("risk-agent",true,.9,"low risk"),
                new AutonomousReleaseEngine.AgentVote("security-agent",true,.9,"clean"),
                new AutonomousReleaseEngine.AgentVote("release-agent",true,.85,"ready"));
        var policy=new GovernanceEngine.PolicyDecision("d1",true,false,.1,"POLICY_PASS");
        var slo=new ReliabilityEngine.SloDecision(true,0,100,"NORMAL");
        var result=a.gate(release,votes,new AutonomousReleaseEngine.SelfUatInput(4,0,true),policy,slo);
        assertEquals("READY",result.decision());
        assertTrue(result.humanOverrideSupported());
    }
}
