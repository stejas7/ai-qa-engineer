package com.aiqa.agent;

import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Applies deterministic safety policies to agent actions before they reach external tools.
 *
 * <p>The policy boundary deliberately does not use an LLM. This keeps authorization decisions
 * predictable, testable and auditable. Sensitive production actions and dangerous tools require
 * explicit human approval.</p>
 */
@Service
public class AgentPolicyService {

    /**
     * Evaluates whether an agent action may execute in the requested environment.
     *
     * @param action requested action, for example {@code EXECUTE_TEST} or {@code DEPLOY}
     * @param tool tool that would perform the action, for example {@code PLAYWRIGHT} or {@code CI_CD}
     * @param environment target environment, for example {@code UAT} or {@code PRODUCTION}
     * @return deterministic policy decision containing the normalized request and reason
     */
    public AgentPolicyDecision evaluate(String action, String tool, String environment) {
        String normalizedAction = value(action);
        String normalizedTool = value(tool);
        String normalizedEnvironment = value(environment);

        if (normalizedAction.isBlank() || normalizedTool.isBlank()) {
            return new AgentPolicyDecision(
                    normalizedAction,
                    normalizedTool,
                    normalizedEnvironment,
                    "DENY",
                    "action and tool are required",
                    false);
        }

        if (isProduction(normalizedEnvironment) && isDestructive(normalizedAction, normalizedTool)) {
            return new AgentPolicyDecision(
                    normalizedAction,
                    normalizedTool,
                    normalizedEnvironment,
                    "APPROVAL_REQUIRED",
                    "Destructive production actions require human approval",
                    true);
        }

        if (isDangerousTool(normalizedTool)) {
            return new AgentPolicyDecision(
                    normalizedAction,
                    normalizedTool,
                    normalizedEnvironment,
                    "APPROVAL_REQUIRED",
                    "Sensitive tools require explicit human approval",
                    true);
        }

        if (isUat(normalizedEnvironment) && isAllowedUatAction(normalizedAction, normalizedTool)) {
            return new AgentPolicyDecision(
                    normalizedAction,
                    normalizedTool,
                    normalizedEnvironment,
                    "ALLOW",
                    "Deterministic UAT action permitted by default policy",
                    false);
        }

        return new AgentPolicyDecision(
                normalizedAction,
                normalizedTool,
                normalizedEnvironment,
                "DENY",
                "No policy rule permits this action",
                false);
    }

    private boolean isAllowedUatAction(String action, String tool) {
        return action.equals("EXECUTE_TEST")
                || action.equals("GENERATE_AUTOMATION")
                || action.equals("ANALYZE_FAILURE")
                || action.equals("READ")
                || tool.equals("PLAYWRIGHT")
                || tool.equals("REPORTING");
    }

    private boolean isDestructive(String action, String tool) {
        return action.contains("DELETE")
                || action.contains("DEPLOY")
                || action.contains("MIGRATE")
                || action.contains("RESET")
                || action.contains("WRITE")
                || tool.contains("CLOUD");
    }

    private boolean isDangerousTool(String tool) {
        return tool.contains("SHELL")
                || tool.contains("SSH")
                || tool.contains("SECRETS")
                || tool.contains("PRODUCTION")
                || tool.contains("DATABASE_ADMIN");
    }

    private boolean isProduction(String environment) {
        return environment.equals("PROD") || environment.equals("PRODUCTION");
    }

    private boolean isUat(String environment) {
        return environment.equals("UAT")
                || environment.equals("TEST")
                || environment.equals("QA");
    }

    private String value(String input) {
        return input == null ? "" : input.trim().toUpperCase(Locale.ROOT);
    }
}
