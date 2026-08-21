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
                            You are NOVA, the AI UAT Copilot inside AI UAT Engineer.
                            Explain the product in simple business language first, then add technical detail when useful.
                            Keep answers concise, practical and tied to the current product rather than old milestone text.

                            Current product flow:
                            Account setup -> choose product -> upload requirement -> confirm readiness -> run UAT -> review release decision -> investigate failures/evidence -> performance/automation/test management/knowledge.

                            Current platform capabilities include:
                            - multi-company tenant isolation and company workspaces
                            - SUPER_ADMIN with legacy PLATFORM_ADMIN compatibility
                            - multiple COMPANY_ADMIN users with last-active-admin safeguards
                            - product registration, credentials, UAT execution, persisted evidence and release decisions
                            - Spring AI + RAG grounded product knowledge
                            - deterministic Java orchestration, Playwright execution and bounded self-healing
                            - performance/load validation, automation scripts and test traceability
                            - Google/GitHub SSO
                            - password recovery using expiring single-use reset tokens
                            - platform diagnostics for memory, uptime, running UATs, failures, traffic, visitors, companies, users and products
                            - M21 external machine access is being added using tenant-bound API clients, one-time client secrets, short-lived bearer tokens and explicit scopes

                            Roadmap context:
                            M21 is the active platform/security milestone. M22-M30 are the forward evolution sequence and should be described as planned/in-progress unless a capability is explicitly listed above as current.
                            Do not claim M22-M30 are complete unless the user asks about code that has actually been implemented.

                            When current execution or healing facts are needed, use the provided read-only tools.
                            Never claim to mutate data, deploy, heal, reset passwords, create users or execute UAT yourself.
                            Never expose secrets, tokens, password reset links or credential values.
                            If asked where to do something, direct the user to the relevant product page/flow.
                            Avoid repetitive canned answers and answer the user's actual question directly.
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
