package com.aiqa.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_approvals")
public class AgentApproval {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private UUID runId;
    @Column(nullable = false) private String action;
    @Column(nullable = false) private String tool;
    @Column(nullable = false) private String environment;
    @Column(nullable = false) private String status;
    @Column(length = 2000) private String reason;
    @Column(length = 2000) private String decisionNote;
    @Column(nullable = false) private Instant createdAt;
    private Instant decidedAt;

    protected AgentApproval() {}

    public AgentApproval(UUID runId, String action, String tool, String environment, String reason) {
        this.runId = runId;
        this.action = action;
        this.tool = tool;
        this.environment = environment;
        this.reason = reason;
        this.status = "PENDING";
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getRunId() { return runId; }
    public String getAction() { return action; }
    public String getTool() { return tool; }
    public String getEnvironment() { return environment; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public String getDecisionNote() { return decisionNote; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getDecidedAt() { return decidedAt; }

    public void approve(String note) { status = "APPROVED"; decisionNote = note; decidedAt = Instant.now(); }
    public void reject(String note) { status = "REJECTED"; decisionNote = note; decidedAt = Instant.now(); }
}
