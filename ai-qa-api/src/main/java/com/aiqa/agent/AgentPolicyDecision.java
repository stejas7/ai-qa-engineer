package com.aiqa.agent;

public record AgentPolicyDecision(
        String action,
        String tool,
        String environment,
        String decision,
        String reason,
        boolean approvalRequired) {

    public boolean allowed() {
        return "ALLOW".equals(decision);
    }
}
