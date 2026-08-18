package com.aiqa.failure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Failure diagnosis using Spring AI ChatClient with deterministic fallback.
 *
 * @author Tejas Shah
 */
@Service
public class FailureAnalysisService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ChatClient chatClient;

    public FailureAnalysisService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a senior QA failure-analysis agent. Return valid JSON only when JSON is requested.")
                .build();
    }

    public FailureAnalysisResponse analyze(FailureAnalysisRequest request) {
        try {
            String prompt = """
                    Analyze this failed UAT test. Return ONLY JSON with fields:
                    classification,severity,probableCause,recommendation,retryRecommended.
                    classification must be one of APPLICATION_OR_REQUIREMENT_DEFECT,
                    AUTOMATION_OR_APPLICATION_DEFECT, ENVIRONMENT_OR_PERFORMANCE, TEST_DATA, UNKNOWN.

                    TestId: %s
                    URL: %s
                    Expected: %s
                    Error: %s
                    """.formatted(request.testId(), request.url(), request.expectedResult(), request.errorMessage());

            String content = chatClient.prompt().user(prompt).call().content();
            if (content == null || content.isBlank()) throw new IllegalStateException("Spring AI returned empty content");
            JsonNode node = mapper.readTree(stripCodeFence(content));
            return new FailureAnalysisResponse(
                    request.testId(),
                    node.path("classification").asText("UNKNOWN"),
                    node.path("severity").asText("MEDIUM"),
                    node.path("probableCause").asText(),
                    node.path("recommendation").asText(),
                    node.path("retryRecommended").asBoolean(false));
        } catch (Exception e) {
            return deterministic(request);
        }
    }

    private String stripCodeFence(String value) {
        String text = value.trim();
        if (text.startsWith("```")) {
            int firstNewLine = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstNewLine >= 0 && lastFence > firstNewLine) text = text.substring(firstNewLine + 1, lastFence).trim();
        }
        return text;
    }

    private FailureAnalysisResponse deterministic(FailureAnalysisRequest request) {
        String error = request.errorMessage() == null ? "" : request.errorMessage().toLowerCase(Locale.ROOT);
        if (error.contains("timeout") || error.contains("timed out"))
            return new FailureAnalysisResponse(request.testId(), "ENVIRONMENT_OR_PERFORMANCE", "MEDIUM",
                    "The page or locator did not become ready within the timeout.",
                    "Check UAT availability and network latency; then retry once before creating a defect.", true);
        if (error.contains("locator") || error.contains("strict mode") || error.contains("not found") || error.contains("element"))
            return new FailureAnalysisResponse(request.testId(), "AUTOMATION_OR_APPLICATION_DEFECT", "HIGH",
                    "The expected UI element could not be located or matched.",
                    "Inspect the DOM and compare the locator with the current UI. If the UI changed intentionally, update the test; otherwise raise an application defect.", false);
        if (error.contains("expected") || error.contains("assert") || error.contains("welcome"))
            return new FailureAnalysisResponse(request.testId(), "APPLICATION_OR_REQUIREMENT_DEFECT", "HIGH",
                    "The observed UI state differs from the expected business outcome.",
                    "Capture the evidence, compare the requirement with current UAT behavior, and create a defect if the requirement is still valid.", false);
        return new FailureAnalysisResponse(request.testId(), "UNKNOWN", "MEDIUM",
                "The failure needs additional evidence before root cause can be determined.",
                "Review screenshot, browser trace, application logs and test data, then retry in the same environment.", true);
    }
}
