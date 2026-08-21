package com.aiqa.externalapi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Short-lived opaque bearer token for external machine access. */
@Entity
@Table(name = "ai_uat_api_tokens")
public class ApiAccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID apiClientId;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false, length = 500)
    private String scopes;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ApiAccessToken() {}

    public ApiAccessToken(UUID apiClientId, UUID companyId, String tokenHash, String scopes, Instant expiresAt) {
        this.apiClientId = apiClientId;
        this.companyId = companyId;
        this.tokenHash = tokenHash;
        this.scopes = scopes;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public UUID getApiClientId() { return apiClientId; }
    public UUID getCompanyId() { return companyId; }
    public String getTokenHash() { return tokenHash; }
    public String getScopes() { return scopes; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public Instant getCreatedAt() { return createdAt; }
    public void revoke() { revoked = true; }
}
