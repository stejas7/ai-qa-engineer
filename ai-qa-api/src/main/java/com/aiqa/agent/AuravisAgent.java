package com.aiqa.agent;

/**
 * Common contract for every specialized Auravis M5 agent.
 *
 * @param <I> agent input type
 * @param <O> agent output type
 * @author Tejas Shah
 */
public interface AuravisAgent<I, O> {
    String name();
    AgentResult<O> execute(AgentContext context, I input);
}
