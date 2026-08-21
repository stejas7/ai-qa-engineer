package com.aiqa.release;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Human release-governance decision linked to one persisted UAT run. */
@Entity
@Table(name = "release_approvals", indexes = {
        @Index(name = "idx_release_approval_company", columnList = "companyId"),
        @Index(name = "idx_release_approval_run", columnList = "runId")
})
public class ReleaseApproval {
    public enum Decision { PENDING, APPROVED, BLOCKED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private UUID runId;

    @Column(nullable = false, length = 320)
    private String requestedBy;

    @Column(length = 320)
    private String decidedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Decision decision = Decision.PENDING;

    @Column(length = 1200)
    private String note;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant decidedAt;

    protected ReleaseApproval() {}

    public ReleaseApproval(UUID companyId, UUID runId, String requestedBy, String note) {
        this.companyId = companyId;
        this.runId = runId;
        this.requestedBy = requestedBy;
        this.note = note;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getRunId() { return runId; }
    public String getRequestedBy() { return requestedBy; }
    public String getDecidedBy() { return decidedBy; }
    public Decision getDecision() { return decision; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getDecidedAt() { return decidedAt; }

    public void decide(Decision decision, String decidedBy, String note) {
        if (decision == null || decision == Decision.PENDING) throw new IllegalArgumentException("Final decision must be APPROVED or BLOCKED");
        if (this.decision != Decision.PENDING) throw new IllegalStateException("Release approval is already final");
        this.decision = decision;
        this.decidedBy = decidedBy;
        this.note = note;
        this.decidedAt = Instant.now();
    }
}
