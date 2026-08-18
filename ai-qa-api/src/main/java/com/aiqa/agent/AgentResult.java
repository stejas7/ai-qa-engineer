package com.aiqa.agent;

/** Typed, auditable outcome returned by an Auravis agent. @author Tejas Shah */
public record AgentResult<T>(boolean success, T value, String summary) {
    public static <T> AgentResult<T> success(T value, String summary) {
        return new AgentResult<>(true, value, summary);
    }
    public static <T> AgentResult<T> failure(String summary) {
        return new AgentResult<>(false, null, summary);
    }
}
