package com.aiqa.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Safe, non-secret runtime metadata for the Spring AI integration.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/ai")
public class AiRuntimeController {

    @Value("${spring.ai.openai.chat.options.model:unknown}")
    private String model;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @GetMapping("/runtime")
    public Map<String, Object> runtime() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("framework", "Spring AI");
        status.put("springAiVersion", "1.1.8");
        status.put("provider", "OpenAI");
        status.put("model", model);
        status.put("configured", apiKey != null && !apiKey.isBlank() && !"demo-key".equals(apiKey));
        status.put("fallback", "Deterministic Java services remain active when the model is unavailable");
        return status;
    }
}
