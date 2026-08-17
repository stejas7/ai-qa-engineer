package com.aiqa.agent;

public interface AgentTool {
    String name();
    AgentToolResult execute(String input);
}
