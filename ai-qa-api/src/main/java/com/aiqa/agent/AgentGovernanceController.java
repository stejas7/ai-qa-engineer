package com.aiqa.agent;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/governance")
public class AgentGovernanceController {
    private final AgentGovernanceService governance;

    public AgentGovernanceController(AgentGovernanceService governance) { this.governance = governance; }

    public record PolicyRequest(String action, String tool, String environment) {}
    public record ApprovalRequest(UUID runId, String action, String tool, String environment) {}
    public record DecisionRequest(boolean approve, String note) {}

    @PostMapping("/policy/evaluate")
    public AgentPolicyDecision evaluate(@RequestBody PolicyRequest request) {
        return governance.evaluate(request.action(), request.tool(), request.environment());
    }

    @PostMapping("/approvals")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentApproval request(@RequestBody ApprovalRequest request) {
        return governance.requestApproval(request.runId(), request.action(), request.tool(), request.environment());
    }

    @GetMapping("/approvals/pending")
    public List<AgentApproval> pending() { return governance.pending(); }

    @GetMapping("/runs/{runId}/approvals")
    public List<AgentApproval> forRun(@PathVariable UUID runId) { return governance.forRun(runId); }

    @PostMapping("/approvals/{approvalId}/decision")
    public AgentApproval decide(@PathVariable UUID approvalId, @RequestBody DecisionRequest request) {
        return governance.decide(approvalId, request.approve(), request.note());
    }
}
