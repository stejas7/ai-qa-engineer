package com.aiqa.requirement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Requirement intelligence powered through Spring AI ChatClient with a deterministic fallback.
 *
 * @author Tejas Shah
 */
@Service
public class AiRequirementService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ChatClient chatClient;

    public AiRequirementService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a senior QA architect. Return concise, valid JSON only when JSON is requested.")
                .build();
    }

    public RequirementAnalysis analyze(Requirement requirement) {
        try {
            String prompt = """
                    Analyze this software requirement for UAT.
                    Return ONLY valid JSON with fields summary, businessRules, questions, testScenarios.
                    Each testScenario must contain id,title,type,priority,steps,expectedResult.

                    Title: %s
                    Description: %s
                    Acceptance criteria: %s
                    """.formatted(requirement.getTitle(), requirement.getDescription(), requirement.getAcceptanceCriteria());

            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            if (content == null || content.isBlank()) throw new IllegalStateException("Spring AI returned empty content");
            return mapper.readValue(stripCodeFence(content), RequirementAnalysis.class);
        } catch (Exception e) {
            return deterministicFallback(requirement);
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

    private RequirementAnalysis deterministicFallback(Requirement requirement) {
        return new RequirementAnalysis(
                requirement.getDescription(),
                requirement.getAcceptanceCriteria(),
                List.of("Confirm all business rules and error messages with the product owner."),
                List.of(
                        new TestScenario("TC-001", "Happy path", "FUNCTIONAL", "HIGH",
                                List.of("Prepare valid data", "Execute business flow", "Verify result"),
                                "Business flow completes successfully."),
                        new TestScenario("TC-002", "Invalid input", "NEGATIVE", "HIGH",
                                List.of("Prepare invalid data", "Execute business flow"),
                                "Invalid input is rejected with a clear error."),
                        new TestScenario("TC-003", "Boundary validation", "BOUNDARY", "MEDIUM",
                                List.of("Use minimum and maximum allowed values", "Execute business flow"),
                                "Boundary values are handled according to the requirement.")));
    }
}
