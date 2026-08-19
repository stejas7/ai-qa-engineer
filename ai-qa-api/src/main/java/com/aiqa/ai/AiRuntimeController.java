package com.aiqa.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Safe, non-secret Spring AI runtime metadata and product-guide endpoint.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/ai")
public class AiRuntimeController {
    private final SpringAiQaAssistantService assistant;

    @Value("${spring.ai.openai.chat.options.model:unknown}")
    private String model;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public AiRuntimeController(SpringAiQaAssistantService assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/runtime")
    public Map<String, Object> runtime() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("framework", "Spring AI");
        status.put("springAiVersion", "1.1.8");
        status.put("provider", "OpenAI");
        status.put("model", model);
        status.put("configured", apiKey != null && !apiKey.isBlank() && !"demo-key".equals(apiKey));
        status.put("chatClient", true);
        status.put("toolCalling", true);
        status.put("qaTools", 2);
        status.put("m7Status", "COMPLETED");
        status.put("m7Focus", "Spring AI runtime, controlled QA tools, RAG and regression intelligence foundation");
        status.put("fallback", "Deterministic Java services remain active when the model is unavailable");
        return status;
    }

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody AskRequest request) {
        if (request == null || request.question() == null || request.question().isBlank()) {
            throw new IllegalArgumentException("question is required");
        }
        return Map.of("answer", assistant.ask(request.question().trim()));
    }

    public record AskRequest(String question) {}
}
