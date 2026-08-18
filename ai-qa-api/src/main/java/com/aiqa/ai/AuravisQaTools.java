package com.aiqa.ai;

import com.aiqa.execution.ExecutionRecordRepository;
import com.aiqa.healing.HealingAttemptRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read-only Spring AI tool registry for M7.
 *
 * <p>These tools expose persisted QA facts to the model while keeping all state-changing
 * operations behind deterministic Java services and explicit product workflows.</p>
 *
 * @author Tejas Shah
 */
@Component
public class AuravisQaTools {
    private final ExecutionRecordRepository executions;
    private final HealingAttemptRepository healingAttempts;

    public AuravisQaTools(ExecutionRecordRepository executions,
                          HealingAttemptRepository healingAttempts) {
        this.executions = executions;
        this.healingAttempts = healingAttempts;
    }

    @Tool(description = "Get current persisted Auravis UAT execution totals, pass/fail counts and pass rate")
    public Map<String, Object> getExecutionSummary() {
        long total = executions.count();
        long passed = executions.countByStatusIgnoreCase("PASS");
        long failed = executions.countByStatusIgnoreCase("FAIL");
        double passRate = total == 0 ? 0.0 : Math.round(passed * 1000.0 / total) / 10.0;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("passed", passed);
        result.put("failed", failed);
        result.put("passRate", passRate);
        return result;
    }

    @Tool(description = "Get current persisted Auravis M6 self-healing decision statistics")
    public Map<String, Object> getHealingSummary() {
        long total = healingAttempts.count();
        long allowed = healingAttempts.countByDecisionIgnoreCase("AUTO_HEAL_ALLOWED");
        long blocked = healingAttempts.countByDecisionIgnoreCase("NO_AUTO_HEAL");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalAttempts", total);
        result.put("autoHealAllowed", allowed);
        result.put("blocked", blocked);
        result.put("autoHealRate", total == 0 ? 0.0 : Math.round(allowed * 1000.0 / total) / 10.0);
        return result;
    }
}
