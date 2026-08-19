package com.aiqa.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Spring AI product guide that can reason over live QA facts through controlled read-only tools.
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
                            You are NOVA, the product guide for AI UAT Engineer.
                            Explain the product in simple business language first, then add technical detail only when useful.
                            Help QA engineers, developers, product owners and release managers understand how requirements
                            become UAT coverage, execution evidence, failure diagnosis and release confidence.
                            You may also explain the roadmap: M1-M10 are complete and M11 is planned for performance/load testing.
                            When current execution or healing facts are needed, use the provided read-only tools.
                            Never claim to mutate data, deploy, heal, or execute anything yourself.
                            Avoid repetitive canned answers; answer the user's actual question directly.
                            """)
                    .user(question)
                    .tools(tools)
                    .call()
                    .content();
            return response == null || response.isBlank()
                    ? "AI guidance is temporarily unavailable. Deterministic AI UAT Engineer services remain active."
                    : response;
        } catch (Exception e) {
            return "AI guidance is temporarily unavailable. Deterministic AI UAT Engineer services remain active.";
        }
    }
}
