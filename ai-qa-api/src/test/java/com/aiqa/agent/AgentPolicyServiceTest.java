package com.aiqa.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentPolicyServiceTest {
    private final AgentPolicyService service = new AgentPolicyService();

    @Test
    void allowsPlaywrightExecutionInUat() {
        AgentPolicyDecision decision = service.evaluate("EXECUTE_TEST", "PLAYWRIGHT", "UAT");
        assertTrue(decision.allowed());
        assertEquals("ALLOW", decision.decision());
    }

    @Test
    void requiresApprovalForProductionDeploy() {
        AgentPolicyDecision decision = service.evaluate("DEPLOY", "CI_CD", "PRODUCTION");
        assertEquals("APPROVAL_REQUIRED", decision.decision());
        assertTrue(decision.approvalRequired());
    }

    @Test
    void deniesUnknownAction() {
        AgentPolicyDecision decision = service.evaluate("RUN_UNKNOWN", "UNKNOWN", "UAT");
        assertEquals("DENY", decision.decision());
    }
}
