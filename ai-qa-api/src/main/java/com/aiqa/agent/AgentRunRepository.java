package com.aiqa.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AgentRunRepository extends JpaRepository<AgentRun, UUID> {}
