package com.aiqa.credential;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

/**
 * M17 credential metadata. Raw secrets are never persisted in this entity.
 */
@Entity
@Table(name = "ai_uat_credential_profiles")
public class CredentialProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false, unique = true)
    private UUID applicationTargetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CredentialType type;

    @JsonIgnore
    @Column(nullable = false)
    private String secretReference;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected CredentialProfile() {}

    public CredentialProfile(UUID companyId, UUID applicationTargetId, CredentialType type, String secretReference) {
        this.companyId = companyId;
        this.applicationTargetId = applicationTargetId;
        this.type = type;
        this.secretReference = secretReference;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getApplicationTargetId() { return applicationTargetId; }
    public CredentialType getType() { return type; }
    public String getSecretReference() { return secretReference; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setActive(boolean active) { this.active = active; }

    public enum CredentialType {
        USERNAME_PASSWORD,
        API_TOKEN,
        OAUTH_CLIENT
    }
}
