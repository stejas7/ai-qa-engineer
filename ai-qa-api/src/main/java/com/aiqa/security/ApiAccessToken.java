package com.aiqa.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Hashed short-lived bearer token issued from tenant API client credentials. */
@Entity
@Table(name = "ai_uat_api_tokens")
public class ApiAccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private UUID apiClientId;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false, length = 1000)
    private String scopes;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ApiAccessToken() {}

    public ApiAccessToken(UUID companyId, UUID apiClientId, String tokenHash, String scopes, Instant expiresAt) {
        this.companyId = companyId;
        this.apiClientId = apiClientId;
        this.tokenHash = tokenHash;
        this.scopes = scopes;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getApiClientId() { return apiClientId; }
    public String getTokenHash() { return tokenHash; }
    public String getScopes() { return scopes; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public Instant getCreatedAt() { return createdAt; }
    public void revoke() { this.revoked = true; }
}
