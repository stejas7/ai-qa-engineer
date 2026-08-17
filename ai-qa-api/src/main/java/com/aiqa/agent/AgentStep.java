package com.aiqa.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_steps")
public class AgentStep {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable=false) private UUID agentRunId;
    @Column(nullable=false) private int sequenceNo;
    @Column(nullable=false) private String stepType;
    @Column(nullable=false) private String status;
    @Column(length=2000) private String input;
    @Column(length=4000) private String output;
    @Column(nullable=false) private Instant createdAt;

    protected AgentStep() {}
    public AgentStep(UUID agentRunId, int sequenceNo, String stepType, String input) {
        this.agentRunId=agentRunId; this.sequenceNo=sequenceNo; this.stepType=stepType; this.input=input;
        this.status="CREATED"; this.createdAt=Instant.now();
    }
    public UUID getId(){return id;} public UUID getAgentRunId(){return agentRunId;} public int getSequenceNo(){return sequenceNo;}
    public String getStepType(){return stepType;} public String getStatus(){return status;} public String getInput(){return input;}
    public String getOutput(){return output;} public Instant getCreatedAt(){return createdAt;}
    public void complete(String output){this.status="COMPLETED";this.output=output;}
    public void fail(String output){this.status="FAILED";this.output=output;}
}
