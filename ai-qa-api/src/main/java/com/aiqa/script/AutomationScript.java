package com.aiqa.script;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Persisted M12 automation script asset bound to one company/product. */
@Entity
@Table(name = "ai_uat_automation_scripts")
public class AutomationScript {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private String status = "DRAFT";

    @ElementCollection
    private List<String> steps = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AutomationScript() {}

    public AutomationScript(UUID companyId, UUID productId, String name, List<String> steps) {
        this.companyId = companyId;
        this.productId = productId;
        this.name = name;
        this.steps = new ArrayList<>(steps);
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getProductId() { return productId; }
    public String getName() { return name; }
    public int getVersion() { return version; }
    public String getStatus() { return status; }
    public List<String> getSteps() { return List.copyOf(steps); }
    public Instant getCreatedAt() { return createdAt; }
    public void approve() { this.status = "APPROVED"; }
}
