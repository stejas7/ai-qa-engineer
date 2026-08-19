package com.aiqa.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Persisted company/product-scoped UAT session. */
@Entity
@Table(name = "auravis_uat_sessions")
public class UatSession {
    private static final Map<UatSessionStatus, Set<UatSessionStatus>> ALLOWED_TRANSITIONS = Map.of(
            UatSessionStatus.CREATED, EnumSet.of(UatSessionStatus.RUNNING, UatSessionStatus.CANCELLED),
            UatSessionStatus.RUNNING, EnumSet.of(UatSessionStatus.COMPLETED, UatSessionStatus.FAILED,
                    UatSessionStatus.CANCELLED),
            UatSessionStatus.COMPLETED, EnumSet.noneOf(UatSessionStatus.class),
            UatSessionStatus.FAILED, EnumSet.noneOf(UatSessionStatus.class),
            UatSessionStatus.CANCELLED, EnumSet.noneOf(UatSessionStatus.class));

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private UUID applicationId;

    @Column(nullable = false)
    private String buildVersion;

    @Column(nullable = false, length = 2000)
    private String objective;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UatSessionStatus status = UatSessionStatus.CREATED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant startedAt;
    private Instant finishedAt;

    protected UatSession() {
    }

    public UatSession(UUID companyId, UUID applicationId, String buildVersion, String objective) {
        this.companyId = companyId;
        this.applicationId = applicationId;
        this.buildVersion = buildVersion == null || buildVersion.isBlank() ? "unspecified" : buildVersion.trim();
        this.objective = objective.trim();
    }

    /**
     * Moves this session to a valid next lifecycle state.
     * Repeating the current state is intentionally idempotent.
     */
    public void transitionTo(UatSessionStatus targetStatus) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("targetStatus is required");
        }
        if (status == targetStatus) {
            return;
        }
        if (!ALLOWED_TRANSITIONS.get(status).contains(targetStatus)) {
            throw new IllegalStateException("Invalid UAT session transition: " + status + " -> " + targetStatus);
        }

        status = targetStatus;
        Instant now = Instant.now();
        if (targetStatus == UatSessionStatus.RUNNING && startedAt == null) {
            startedAt = now;
        }
        if (targetStatus == UatSessionStatus.COMPLETED
                || targetStatus == UatSessionStatus.FAILED
                || targetStatus == UatSessionStatus.CANCELLED) {
            finishedAt = now;
        }
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getApplicationId() { return applicationId; }
    public String getBuildVersion() { return buildVersion; }
    public String getObjective() { return objective; }
    public UatSessionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
}
