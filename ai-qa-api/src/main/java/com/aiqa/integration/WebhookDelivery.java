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

/** Persisted outbound webhook delivery metadata. */
@Entity
@Table(name = "webhook_deliveries", indexes = {
        @Index(name = "idx_webhook_delivery_endpoint", columnList = "endpointId"),
        @Index(name = "idx_webhook_delivery_created", columnList = "createdAt")
})
public class WebhookDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID endpointId;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false)
    private int statusCode;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 500)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected WebhookDelivery() {}

    public WebhookDelivery(UUID endpointId, UUID companyId, String eventType, int statusCode, boolean success, String message) {
        this.endpointId = endpointId;
        this.companyId = companyId;
        this.eventType = eventType;
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
    }

    public UUID getId() { return id; }
    public UUID getEndpointId() { return endpointId; }
    public UUID getCompanyId() { return companyId; }
    public String getEventType() { return eventType; }
    public int getStatusCode() { return statusCode; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
