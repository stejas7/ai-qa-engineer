package com.aiqa.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * M7 Spring AI assistant that can reason over live Auravis QA facts through controlled read-only tools.
 *
 * @author Tejas Shah
 */
@Service
public class SpringAiQaAssistantService {
    private final ChatClient chatClient;
    private final AuravisQaTools tools;

    public SpringAiQaAssistantService(ChatClient.Builder builder, AuravisQaTools tools) {
        this.chatClient = builder.build();
        this.tools = tools;
    }

    public String ask(String question) {
        try {
            String response = chatClient.prompt()
                    .system("""
                            You are Auravis M7 QA Intelligence. Answer only about software QA, UAT,
                            Auravis execution results, healing statistics, regression risk and release confidence.
                            When current execution or healing facts are needed, use the provided tools.
                            Never claim to execute, mutate, deploy or heal anything yourself.
                            """)
                    .user(question)
                    .tools(tools)
                    .call()
                    .content();
            return response == null || response.isBlank()
                    ? "Spring AI returned no content. Deterministic Auravis services remain available."
                    : response;
        } catch (Exception e) {
            return "Spring AI is temporarily unavailable. Deterministic Auravis services remain active.";
        }
    }
}
