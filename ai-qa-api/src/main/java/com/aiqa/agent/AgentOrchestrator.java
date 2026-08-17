package com.aiqa.agent;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class AgentOrchestrator {
    private final AgentRunRepository runs;
    private final AgentStepRepository steps;

    public AgentOrchestrator(AgentRunRepository runs, AgentStepRepository steps) {
        this.runs=runs; this.steps=steps;
    }

    @Transactional
    public AgentRun start(String agentType, String input) {
        AgentRun run = runs.save(new AgentRun(agentType, input));
        run.start();
        return runs.save(run);
    }

    @Transactional
    public AgentStep addStep(UUID runId, String type, String input) {
        int next = steps.findByAgentRunIdOrderBySequenceNo(runId).size() + 1;
        return steps.save(new AgentStep(runId, next, type, input));
    }

    @Transactional
    public AgentStep completeStep(UUID stepId, String output) {
        AgentStep step = steps.findById(stepId).orElseThrow(() -> new IllegalArgumentException("Agent step not found"));
        step.complete(output); return steps.save(step);
    }

    @Transactional
    public AgentRun complete(UUID runId, String summary) {
        AgentRun run = runs.findById(runId).orElseThrow(() -> new IllegalArgumentException("Agent run not found"));
        run.complete(summary); return runs.save(run);
    }

    public AgentRun getRun(UUID id) { return runs.findById(id).orElseThrow(() -> new IllegalArgumentException("Agent run not found")); }
    public List<AgentStep> getSteps(UUID id) { return steps.findByAgentRunIdOrderBySequenceNo(id); }
    public List<AgentRun> getRuns() { return runs.findAll(); }
}
