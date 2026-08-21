package com.aiqa.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Tenant-bound machine identity for M21 external API access. */
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
    private String secretHash;

    @Column(nullable = false, length = 1000)
    private String scopes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ApiClient() {}

    public ApiClient(UUID companyId, String name, String clientId, String secretHash, String scopes) {
        this.companyId = companyId;
        this.name = name;
        this.clientId = clientId;
        this.secretHash = secretHash;
        this.scopes = scopes;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getName() { return name; }
    public String getClientId() { return clientId; }
    public String getSecretHash() { return secretHash; }
    public String getScopes() { return scopes; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void deactivate() { this.active = false; }
}
