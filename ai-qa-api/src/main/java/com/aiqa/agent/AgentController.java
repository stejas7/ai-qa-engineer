package com.aiqa.agent;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    private final AgentOrchestrator orchestrator;
    private final AgentPipelineService pipeline;

    public AgentController(AgentOrchestrator orchestrator, AgentPipelineService pipeline) {
        this.orchestrator = orchestrator;
        this.pipeline = pipeline;
    }

    public record StartRequest(String agentType, String input) {}
    public record StepRequest(String stepType, String input) {}
    public record CompleteStepRequest(String output) {}
    public record CompleteRunRequest(String summary) {}

    @PostMapping("/runs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AgentRun start(@RequestBody StartRequest request) {
        if (request == null || request.agentType() == null || request.agentType().isBlank())
            throw new IllegalArgumentException("agentType is required");
        return orchestrator.start(request.agentType(), request.input());
    }

    @PostMapping("/pipeline")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AgentPipelineService.PipelineResult pipeline(@RequestBody AgentPipelineService.PipelineRequest request) {
        if (request == null || request.title() == null || request.title().isBlank())
            throw new IllegalArgumentException("title is required");
        if (request.url() == null || request.url().isBlank())
            throw new IllegalArgumentException("url is required");
        return pipeline.run(request);
    }

    @GetMapping("/runs") public List<AgentRun> runs() { return orchestrator.getRuns(); }
    @GetMapping("/runs/{id}") public AgentRun run(@PathVariable UUID id) { return orchestrator.getRun(id); }
    @GetMapping("/runs/{id}/steps") public List<AgentStep> steps(@PathVariable UUID id) { return orchestrator.getSteps(id); }

    @PostMapping("/runs/{id}/steps")
    public AgentStep addStep(@PathVariable UUID id, @RequestBody StepRequest request) {
        return orchestrator.addStep(id, request.stepType(), request.input());
    }

    @PostMapping("/steps/{id}/complete")
    public AgentStep completeStep(@PathVariable UUID id, @RequestBody CompleteStepRequest request) {
        return orchestrator.completeStep(id, request.output());
    }

    @PostMapping("/runs/{id}/complete")
    public AgentRun complete(@PathVariable UUID id, @RequestBody CompleteRunRequest request) {
        return orchestrator.complete(id, request.summary());
    }
}
