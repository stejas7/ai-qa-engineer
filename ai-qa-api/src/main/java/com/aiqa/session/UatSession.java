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
import java.util.UUID;

/** Persisted company/product-scoped UAT session. */
@Entity
@Table(name = "auravis_uat_sessions")
public class UatSession {
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

    protected UatSession() {
    }

    public UatSession(UUID companyId, UUID applicationId, String buildVersion, String objective) {
        this.companyId = companyId;
        this.applicationId = applicationId;
        this.buildVersion = buildVersion == null || buildVersion.isBlank() ? "unspecified" : buildVersion.trim();
        this.objective = objective.trim();
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getApplicationId() { return applicationId; }
    public String getBuildVersion() { return buildVersion; }
    public String getObjective() { return objective; }
    public UatSessionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(UatSessionStatus status) { this.status = status; }
}
