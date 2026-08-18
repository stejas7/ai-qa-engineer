package com.aiqa.auravis;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Represents one autonomous end-to-end Auravis UAT mission. */
@Entity
@Table(name = "auravis_missions")
public class AuravisMission {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 30) private String status;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String requirement;
    @Column(nullable = false, length = 1000) private String uatUrl;
    @Column(length = 1000) private String requirementSummary;
    @Column(length = 2000) private String finalDecision;
    private int scenariosGenerated;
    private int testsPassed;
    private int testsFailed;
    @Column(nullable = false) private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    protected AuravisMission() {}

    public AuravisMission(String title, String requirement, String uatUrl) {
        this.title = title;
        this.requirement = requirement;
        this.uatUrl = uatUrl;
        this.status = "CREATED";
        this.createdAt = Instant.now();
    }

    public UUID getId(){ return id; }
    public String getStatus(){ return status; }
    public String getTitle(){ return title; }
    public String getRequirement(){ return requirement; }
    public String getUatUrl(){ return uatUrl; }
    public String getRequirementSummary(){ return requirementSummary; }
    public String getFinalDecision(){ return finalDecision; }
    public int getScenariosGenerated(){ return scenariosGenerated; }
    public int getTestsPassed(){ return testsPassed; }
    public int getTestsFailed(){ return testsFailed; }
    public Instant getCreatedAt(){ return createdAt; }
    public Instant getStartedAt(){ return startedAt; }
    public Instant getCompletedAt(){ return completedAt; }

    void start(){ status = "RUNNING"; startedAt = Instant.now(); }
    void requirementReady(String summary){ requirementSummary = summary; }
    void scenarioCount(int count){ scenariosGenerated = count; }
    void testPassed(){ testsPassed++; }
    void testFailed(){ testsFailed++; }
    void complete(String decision){ status = "COMPLETED"; finalDecision = decision; completedAt = Instant.now(); }
    void fail(String decision){ status = "FAILED"; finalDecision = decision; completedAt = Instant.now(); }
}
