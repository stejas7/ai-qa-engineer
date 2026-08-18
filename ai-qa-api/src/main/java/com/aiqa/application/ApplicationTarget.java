package com.aiqa.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Registered application/environment that Auravis can execute UAT against. */
@Entity
@Table(name = "auravis_application_targets")
public class ApplicationTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String baseUrl;

    @Column(nullable = false)
    private String environment;

    @Column(nullable = false)
    private String authType;

    @Column
    private UUID companyId;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ApplicationTarget() {}

    public ApplicationTarget(String name, String baseUrl, String environment, String authType) {
        this(name, baseUrl, environment, authType, null);
    }

    public ApplicationTarget(String name, String baseUrl, String environment, String authType, UUID companyId) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.environment = environment == null || environment.isBlank() ? "UAT" : environment;
        this.authType = authType == null || authType.isBlank() ? "NONE" : authType;
        this.companyId = companyId;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getBaseUrl() { return baseUrl; }
    public String getEnvironment() { return environment; }
    public String getAuthType() { return authType; }
    public UUID getCompanyId() { return companyId; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setActive(boolean active) { this.active = active; }
}
