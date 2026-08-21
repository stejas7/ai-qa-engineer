package com.aiqa.integration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Tenant-owned outbound integration endpoint. */
@Entity
@Table(name = "integration_endpoints", indexes = {
        @Index(name = "idx_integration_company", columnList = "companyId")
})
public class IntegrationEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 600)
    private String url;

    @Column(nullable = false, length = 300)
    private String eventTypes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected IntegrationEndpoint() {}

    public IntegrationEndpoint(UUID companyId, String name, String url, String eventTypes) {
        this.companyId = companyId;
        this.name = name;
        this.url = url;
        this.eventTypes = eventTypes;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getEventTypes() { return eventTypes; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setActive(boolean active) { this.active = active; }
}
