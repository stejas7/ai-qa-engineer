package com.aiqa.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AgentStepRepository extends JpaRepository<AgentStep, UUID> {
    List<AgentStep> findByAgentRunIdOrderBySequenceNo(UUID agentRunId);
}
