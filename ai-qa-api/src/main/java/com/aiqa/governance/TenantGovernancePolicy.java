package com.aiqa.governance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Configurable per-company safety limits. */
@Entity
@Table(name = "tenant_governance_policies")
public class TenantGovernancePolicy {
    @Id
    private UUID companyId;

    @Column(nullable = false)
    private int maxUsers = 50;

    @Column(nullable = false)
    private int maxProducts = 20;

    @Column(nullable = false)
    private int maxConcurrentUat = 5;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected TenantGovernancePolicy() {}

    public TenantGovernancePolicy(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getCompanyId() { return companyId; }
    public int getMaxUsers() { return maxUsers; }
    public int getMaxProducts() { return maxProducts; }
    public int getMaxConcurrentUat() { return maxConcurrentUat; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(int maxUsers, int maxProducts, int maxConcurrentUat) {
        if (maxUsers < 1 || maxProducts < 1 || maxConcurrentUat < 1) {
            throw new IllegalArgumentException("Governance limits must be at least 1");
        }
        this.maxUsers = Math.min(maxUsers, 10000);
        this.maxProducts = Math.min(maxProducts, 1000);
        this.maxConcurrentUat = Math.min(maxConcurrentUat, 100);
        this.updatedAt = Instant.now();
    }
}
