package com.aiqa.platform;

import org.springframework.stereotype.Service;

import java.util.List;

/** M47-M50 policy-bound autonomous release runtime. */
@Service
public class AutonomousReleaseEngine {
    public ReleaseAssessment assess(ReleaseInput input) {
        if (input == null) throw new IllegalArgumentException("release input is required");
        double risk = clamp(0.30 * input.changeRisk() + 0.25 * input.testFailureRate() + 0.20 * input.flakyRate() + 0.15 * input.securityRisk() + 0.10 * input.performanceRisk());
        String recommendation = risk >= 0.75 ? "BLOCK" : risk >= 0.45 ? "REVIEW" : "READY";
        return new ReleaseAssessment(risk, recommendation, input.securityRisk() >= 0.8, input.performanceRisk() >= 0.8);
    }

    public ConsensusResult consensus(List<AgentVote> votes) {
        if (votes == null || votes.size() < 3) throw new IllegalArgumentException("At least three independent agent votes are required");
        long approve = votes.stream().filter(AgentVote::approve).count();
        double avgConfidence = votes.stream().mapToDouble(AgentVote::confidence).average().orElse(0);
        boolean consensus = approve >= Math.ceil(votes.size() * 0.67) && avgConfidence >= 0.65;
        return new ConsensusResult(consensus, approve, votes.size() - approve, avgConfidence, consensus ? "CONSENSUS_READY" : "ESCALATE_CONFLICT");
    }

    public SelfUatResult selfUat(SelfUatInput input) {
        if (input == null) throw new IllegalArgumentException("self-UAT input is required");
        int generated = Math.max(1, input.requirementCount() * 3);
        int executed = generated;
        int passed = Math.max(0, generated - Math.max(0, input.simulatedFailures()));
        boolean healed = input.simulatedFailures() > 0 && input.healingEnabled();
        boolean successful = passed == generated || healed;
        return new SelfUatResult(generated, executed, passed, generated - passed, healed, successful ? "PASS" : "FAIL");
    }

    public GateDecision gate(ReleaseInput release, List<AgentVote> votes, SelfUatInput selfUatInput, GovernanceEngine.PolicyDecision policy, ReliabilityEngine.SloDecision slo) {
        ReleaseAssessment assessment = assess(release);
        ConsensusResult consensus = consensus(votes);
        SelfUatResult selfUat = selfUat(selfUatInput);
        boolean ready = policy.allowed() && !policy.approvalRequired() && slo.healthy() && assessment.recommendation().equals("READY") && consensus.consensus() && selfUat.successful();
        String decision = ready ? "READY" : "BLOCKED";
        String reason = ready ? "ALL_GATES_PASSED" : String.join(";",
                policy.allowed() ? "POLICY_OK" : "POLICY_BLOCK",
                policy.approvalRequired() ? "APPROVAL_REQUIRED" : "APPROVAL_NOT_REQUIRED",
                slo.healthy() ? "SLO_OK" : "SLO_BREACH",
                "RISK_" + assessment.recommendation(),
                consensus.state(),
                "SELF_UAT_" + selfUat.state());
        return new GateDecision(decision, reason, assessment, consensus, selfUat, true);
    }

    private double clamp(double v){return Math.max(0, Math.min(1, v));}
    public record ReleaseInput(double changeRisk,double testFailureRate,double flakyRate,double securityRisk,double performanceRisk){}
    public record ReleaseAssessment(double riskScore,String recommendation,boolean securityHotspot,boolean performanceHotspot){}
    public record AgentVote(String agentId,boolean approve,double confidence,String rationale){}
    public record ConsensusResult(boolean consensus,long approvals,long rejections,double averageConfidence,String state){}
    public record SelfUatInput(int requirementCount,int simulatedFailures,boolean healingEnabled){}
    public record SelfUatResult(int generated,int executed,int passed,int failed,boolean healed,boolean successful,String state){}
    public record GateDecision(String decision,String reason,ReleaseAssessment assessment,ConsensusResult consensus,SelfUatResult selfUat,boolean humanOverrideSupported){}
}
