package com.aiqa.agent;

public record AgentToolResult(boolean success, String output) {
    public static AgentToolResult ok(String output) { return new AgentToolResult(true, output); }
    public static AgentToolResult failure(String output) { return new AgentToolResult(false, output); }
}
