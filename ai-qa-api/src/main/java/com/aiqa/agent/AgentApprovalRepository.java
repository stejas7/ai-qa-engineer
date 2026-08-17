package com.aiqa.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentApprovalRepository extends JpaRepository<AgentApproval, UUID> {
    List<AgentApproval> findByStatusOrderByCreatedAtDesc(String status);
    List<AgentApproval> findByRunIdOrderByCreatedAtDesc(UUID runId);
}
