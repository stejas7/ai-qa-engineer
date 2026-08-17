package com.aiqa.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_runs")
public class AgentRun {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable=false) private String agentType;
    @Column(nullable=false) private String status;
    @Column(length=2000) private String input;
    @Column(length=4000) private String decisionSummary;
    @Column(nullable=false) private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    protected AgentRun() {}
    public AgentRun(String agentType, String input) {
        this.agentType = agentType; this.input = input; this.status = "CREATED"; this.createdAt = Instant.now();
    }
    public UUID getId(){return id;} public String getAgentType(){return agentType;} public String getStatus(){return status;}
    public String getInput(){return input;} public String getDecisionSummary(){return decisionSummary;}
    public Instant getCreatedAt(){return createdAt;} public Instant getStartedAt(){return startedAt;} public Instant getCompletedAt(){return completedAt;}
    public void start(){status="RUNNING"; startedAt=Instant.now();}
    public void complete(String summary){status="COMPLETED"; decisionSummary=summary; completedAt=Instant.now();}
    public void fail(String summary){status="FAILED"; decisionSummary=summary; completedAt=Instant.now();}
    public void waitForApproval(){status="WAITING_APPROVAL";}
    public void cancel(){status="CANCELLED"; completedAt=Instant.now();}
}
