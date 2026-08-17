package com.aiqa.failure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Map;

@Service
public class FailureAnalysisService {
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${openai.api-key:}") private String apiKey;
    @Value("${openai.model:gpt-4.1-mini}") private String model;

    public FailureAnalysisResponse analyze(FailureAnalysisRequest r) {
        if (apiKey != null && !apiKey.isBlank()) {
            try { return aiAnalyze(r); } catch (Exception ignored) { }
        }
        return deterministic(r);
    }

    private FailureAnalysisResponse deterministic(FailureAnalysisRequest r) {
        String e = r.errorMessage().toLowerCase(Locale.ROOT);
        if (e.contains("timeout") || e.contains("timed out"))
            return new FailureAnalysisResponse(r.testId(), "ENVIRONMENT_OR_PERFORMANCE", "MEDIUM", "The page or locator did not become ready within the timeout.", "Check UAT availability and network latency; then retry once before creating a defect.", true);
        if (e.contains("locator") || e.contains("strict mode") || e.contains("not found") || e.contains("element"))
            return new FailureAnalysisResponse(r.testId(), "AUTOMATION_OR_APPLICATION_DEFECT", "HIGH", "The expected UI element could not be located or matched.", "Inspect the DOM and compare the locator with the current UI. If the UI changed intentionally, update the test; otherwise raise an application defect.", false);
        if (e.contains("expected") || e.contains("assert") || e.contains("welcome"))
            return new FailureAnalysisResponse(r.testId(), "APPLICATION_OR_REQUIREMENT_DEFECT", "HIGH", "The observed UI state differs from the expected business outcome.", "Capture the evidence, compare the requirement with the current UAT behavior, and create a defect if the requirement is still valid.", false);
        return new FailureAnalysisResponse(r.testId(), "UNKNOWN", "MEDIUM", "The failure needs additional evidence before root cause can be determined.", "Review the screenshot, browser trace, application logs and test data, then retry in the same environment.", true);
    }

    private FailureAnalysisResponse aiAnalyze(FailureAnalysisRequest r) throws Exception {
        String prompt = "You are a senior QA failure-analysis agent. Return ONLY JSON with fields classification,severity,probableCause,recommendation,retryRecommended. Classify as APPLICATION_OR_REQUIREMENT_DEFECT, AUTOMATION_OR_APPLICATION_DEFECT, ENVIRONMENT_OR_PERFORMANCE, TEST_DATA, or UNKNOWN. TestId=" + r.testId() + "; URL=" + r.url() + "; Expected=" + r.expectedResult() + "; Error=" + r.errorMessage();
        String body = mapper.writeValueAsString(Map.of("model", model, "input", prompt));
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.openai.com/v1/responses"))
            .header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IllegalStateException("AI HTTP " + response.statusCode());
        JsonNode root = mapper.readTree(response.body());
        for (JsonNode item : root.path("output")) for (JsonNode content : item.path("content")) if (content.has("text")) {
            JsonNode x = mapper.readTree(content.get("text").asText());
            return new FailureAnalysisResponse(r.testId(), x.path("classification").asText("UNKNOWN"), x.path("severity").asText("MEDIUM"), x.path("probableCause").asText(), x.path("recommendation").asText(), x.path("retryRecommended").asBoolean(false));
        }
        throw new IllegalStateException("No AI output");
    }
}
