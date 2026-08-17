package com.aiqa.agent;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Owns the persisted lifecycle of agent runs and their ordered execution steps.
 *
 * <p>The orchestrator is intentionally independent of any particular LLM provider. Agents use it
 * to record workflow state while deterministic services perform the actual work.</p>
 */
@Service
public class AgentOrchestrator {
    private final AgentRunRepository runs;
    private final AgentStepRepository steps;

    /**
     * Creates an orchestrator backed by the run and step repositories.
     *
     * @param runs repository for persisted agent runs
     * @param steps repository for persisted agent steps
     */
    public AgentOrchestrator(AgentRunRepository runs, AgentStepRepository steps) {
        this.runs = runs;
        this.steps = steps;
    }

    /**
     * Creates and starts a new agent run.
     *
     * @param agentType logical workflow or agent type
     * @param input initial workflow input
     * @return persisted running agent run
     */
    @Transactional
    public AgentRun start(String agentType, String input) {
        AgentRun run = runs.save(new AgentRun(agentType, input));
        run.start();
        return runs.save(run);
    }

    /**
     * Adds the next ordered step to an agent run.
     *
     * @param runId owning agent run identifier
     * @param type logical step type
     * @param input step input or description
     * @return persisted agent step
     */
    @Transactional
    public AgentStep addStep(UUID runId, String type, String input) {
        int next = steps.findByAgentRunIdOrderBySequenceNo(runId).size() + 1;
        return steps.save(new AgentStep(runId, next, type, input));
    }

    /**
     * Completes an existing agent step with its deterministic output.
     *
     * @param stepId agent step identifier
     * @param output step result or summary
     * @return persisted completed step
     * @throws IllegalArgumentException when the step does not exist
     */
    @Transactional
    public AgentStep completeStep(UUID stepId, String output) {
        AgentStep step = steps.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Agent step not found"));
        step.complete(output);
        return steps.save(step);
    }

    /**
     * Completes an agent run with a final summary.
     *
     * @param runId agent run identifier
     * @param summary final workflow summary
     * @return persisted completed run
     * @throws IllegalArgumentException when the run does not exist
     */
    @Transactional
    public AgentRun complete(UUID runId, String summary) {
        AgentRun run = runs.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Agent run not found"));
        run.complete(summary);
        return runs.save(run);
    }

    /**
     * Retrieves one persisted agent run.
     *
     * @param id agent run identifier
     * @return persisted run
     * @throws IllegalArgumentException when the run does not exist
     */
    public AgentRun getRun(UUID id) {
        return runs.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent run not found"));
    }

    /**
     * Retrieves the ordered steps belonging to an agent run.
     *
     * @param id agent run identifier
     * @return ordered agent steps
     */
    public List<AgentStep> getSteps(UUID id) {
        return steps.findByAgentRunIdOrderBySequenceNo(id);
    }

    /**
     * Lists all persisted agent runs.
     *
     * @return all known agent runs
     */
    public List<AgentRun> getRuns() {
        return runs.findAll();
    }
}
