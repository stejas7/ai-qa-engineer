package com.aiqa.agent;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AgentGovernanceService {
    private final AgentPolicyService policy;
    private final AgentApprovalRepository approvals;
    private final AgentOrchestrator orchestrator;

    public AgentGovernanceService(AgentPolicyService policy, AgentApprovalRepository approvals, AgentOrchestrator orchestrator) {
        this.policy = policy;
        this.approvals = approvals;
        this.orchestrator = orchestrator;
    }

    public AgentPolicyDecision evaluate(String action, String tool, String environment) {
        return policy.evaluate(action, tool, environment);
    }

    @Transactional
    public AgentApproval requestApproval(UUID runId, String action, String tool, String environment) {
        AgentPolicyDecision decision = policy.evaluate(action, tool, environment);
        if (!decision.approvalRequired()) {
            throw new IllegalArgumentException("Approval is not required for this action");
        }
        AgentApproval approval = approvals.save(new AgentApproval(runId, action, tool, environment, decision.reason()));
        if (runId != null) {
            orchestrator.addStep(runId, "APPROVAL_REQUESTED", action + " / " + tool + " / " + environment);
        }
        return approval;
    }

    @Transactional
    public AgentApproval decide(UUID approvalId, boolean approve, String note) {
        AgentApproval approval = approvals.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found"));
        if (!"PENDING".equals(approval.getStatus())) {
            throw new IllegalStateException("Approval has already been decided");
        }
        if (approve) approval.approve(note); else approval.reject(note);
        return approvals.save(approval);
    }

    public List<AgentApproval> pending() { return approvals.findByStatusOrderByCreatedAtDesc("PENDING"); }
    public List<AgentApproval> forRun(UUID runId) { return approvals.findByRunIdOrderByCreatedAtDesc(runId); }
}
