package com.aiqa.healing;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/** Persisted audit record for every Auravis self-healing decision. @author Tejas Shah */
@Entity
@Table(name = "healing_attempts")
public class HealingAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String testId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FailureCategory category;

    @Column(length = 2000)
    private String originalFailure;

    @Column(length = 1000)
    private String proposedRepair;

    @Column(nullable = false)
    private double confidence;

    @Column(nullable = false)
    private String decision;

    @Column(nullable = false)
    private Instant createdAt;

    protected HealingAttempt() {}

    public HealingAttempt(String testId, FailureCategory category, String originalFailure,
                          String proposedRepair, double confidence, String decision) {
        this.testId = testId;
        this.category = category;
        this.originalFailure = originalFailure;
        this.proposedRepair = proposedRepair;
        this.confidence = confidence;
        this.decision = decision;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTestId() { return testId; }
    public FailureCategory getCategory() { return category; }
    public String getOriginalFailure() { return originalFailure; }
    public String getProposedRepair() { return proposedRepair; }
    public double getConfidence() { return confidence; }
    public String getDecision() { return decision; }
    public Instant getCreatedAt() { return createdAt; }
}
