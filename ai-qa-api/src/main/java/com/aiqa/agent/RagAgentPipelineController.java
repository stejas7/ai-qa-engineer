package com.aiqa.agent;

import com.aiqa.rag.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/agents")
public class RagAgentPipelineController {
    private final AgentOrchestrator orchestrator;
    private final RagService ragService;

    public RagAgentPipelineController(AgentOrchestrator orchestrator, RagService ragService) {
        this.orchestrator = orchestrator;
        this.ragService = ragService;
    }

    @PostMapping("/rag-pipeline")
    public ResponseEntity<?> runWithKnowledge(@RequestBody PipelineRequest request) {
        if (request == null || request.requirement() == null || request.requirement().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "requirement is required"));
        }

        AgentRun run = orchestrator.start("RAG_AGENT_PIPELINE", request.requirement());
        AgentStep retrieval = orchestrator.addStep(run.getId(), "KNOWLEDGE_RETRIEVAL", request.requirement());

        try {
            List<Map<String,Object>> knowledge = ragService.search(request.requirement(), request.limit() == null ? 5 : request.limit());
            String output = knowledge.toString();
            orchestrator.completeStep(retrieval.getId(), output);

            AgentStep grounding = orchestrator.addStep(run.getId(), "AGENT_GROUNDING", "Use retrieved enterprise knowledge to ground the QA workflow");
            String summary = "Retrieved " + knowledge.size() + " knowledge items for agent grounding";
            orchestrator.completeStep(grounding.getId(), summary);
            AgentRun completed = orchestrator.complete(run.getId(), summary);

            return ResponseEntity.ok(Map.of(
                    "runId", completed.getId(),
                    "status", completed.getStatus(),
                    "knowledge", knowledge,
                    "steps", orchestrator.getSteps(completed.getId())
            ));
        } catch (Exception ex) {
            orchestrator.completeStep(retrieval.getId(), "RAG retrieval failed: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("runId", run.getId(), "error", ex.getMessage()));
        }
    }

    public record PipelineRequest(String requirement, Integer limit) {}
}
