package com.aiqa.agent;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** Read-only observability API for persisted M5 agent runs and steps. @author Tejas Shah */
@RestController
@RequestMapping("/api/agent-activity")
public class AgentActivityController {
    private final AgentRunRepository runs;
    private final AgentStepRepository steps;

    public AgentActivityController(AgentRunRepository runs, AgentStepRepository steps) {
        this.runs = runs;
        this.steps = steps;
    }

    @GetMapping("/runs")
    public List<AgentRun> recentRuns(@RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return runs.findAll(PageRequest.of(0, safeLimit, org.springframework.data.domain.Sort.by("createdAt").descending())).getContent();
    }

    @GetMapping("/runs/{runId}/steps")
    public List<AgentStep> runSteps(@PathVariable UUID runId) {
        return steps.findByAgentRunIdOrderBySequenceNo(runId);
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<AgentRun> all = runs.findAll();
        long running = all.stream().filter(r -> "RUNNING".equals(r.getStatus())).count();
        long completed = all.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count();
        long failed = all.stream().filter(r -> "FAILED".equals(r.getStatus())).count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRuns", all.size());
        result.put("running", running);
        result.put("completed", completed);
        result.put("failed", failed);
        result.put("milestone", "M5");
        result.put("status", "COMPLETED");
        result.put("flow", List.of("REQUIREMENT_ANALYSIS", "TEST_DESIGN", "AUTOMATION_GENERATION", "UAT_EXECUTION", "FAILURE_DIAGNOSIS", "QUALITY_DECISION"));
        return result;
    }
}
