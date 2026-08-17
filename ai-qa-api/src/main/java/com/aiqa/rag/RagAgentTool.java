package com.aiqa.rag;

import com.aiqa.agent.AgentTool;
import com.aiqa.agent.AgentToolResult;
import org.springframework.stereotype.Component;

@Component
public class RagAgentTool implements AgentTool {
    private final RagService ragService;

    public RagAgentTool(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public String name() {
        return "enterprise_knowledge_search";
    }

    @Override
    public AgentToolResult execute(String input) {
        return AgentToolResult.ok(ragService.search(input, 5).toString());
    }
}
