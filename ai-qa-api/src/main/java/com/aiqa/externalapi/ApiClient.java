package com.aiqa.externalapi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Tenant-bound machine client used by the M21 external API. */
@Entity
@Table(name = "ai_uat_api_clients")
public class ApiClient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String clientId;

    @Column(nullable = false)
    private String clientSecretHash;

    @Column(nullable = false, length = 500)
    private String scopes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ApiClient() {}

    public ApiClient(UUID companyId, String name, String clientId, String clientSecretHash, String scopes) {
        this.companyId = companyId;
        this.name = name;
        this.clientId = clientId;
        this.clientSecretHash = clientSecretHash;
        this.scopes = scopes;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getName() { return name; }
    public String getClientId() { return clientId; }
    public String getClientSecretHash() { return clientSecretHash; }
    public String getScopes() { return scopes; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void deactivate() { active = false; }
    public void activate() { active = true; }
    public void rotateSecret(String hash) { this.clientSecretHash = hash; }
    public void replaceScopes(String scopes) { this.scopes = scopes; }
}
