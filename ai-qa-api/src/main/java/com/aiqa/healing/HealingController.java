package com.aiqa.healing;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only audit plus controlled decision endpoint for M6 self-healing. @author Tejas Shah */
@RestController
@RequestMapping("/api/healing")
public class HealingController {
    private final HealingDecisionService decisions;
    private final HealingAttemptRepository attempts;

    public HealingController(HealingDecisionService decisions, HealingAttemptRepository attempts) {
        this.decisions = decisions;
        this.attempts = attempts;
    }

    @PostMapping("/evaluate")
    public HealingDecisionService.HealingDecision evaluate(@RequestBody HealingRequest request) {
        return decisions.evaluate(request.testId(), request.failureMessage(), request.proposedRepair(), request.confidence());
    }

    @GetMapping("/history")
    public List<HealingAttempt> history() {
        return attempts.findTop100ByOrderByCreatedAtDesc();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        long total = attempts.count();
        long allowed = attempts.countByDecisionIgnoreCase("AUTO_HEAL_ALLOWED");
        long blocked = attempts.countByDecisionIgnoreCase("NO_AUTO_HEAL");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalAttempts", total);
        result.put("autoHealAllowed", allowed);
        result.put("blocked", blocked);
        result.put("autoHealRate", total == 0 ? 0.0 : Math.round(allowed * 1000.0 / total) / 10.0);
        result.put("milestone", "M6");
        result.put("status", "COMPLETED");
        result.put("policy", "Recoverable automation failures only; confidence >= 0.90; one controlled retry; assertion/business failures are never auto-healed");
        return result;
    }

    public record HealingRequest(String testId, String failureMessage, String proposedRepair, double confidence) {}
}
