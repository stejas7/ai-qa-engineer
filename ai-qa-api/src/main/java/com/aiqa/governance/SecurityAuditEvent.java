package com.aiqa.governance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/** Durable metadata-only audit event for authenticated API mutations. */
@Entity
@Table(name = "security_audit_events", indexes = {
        @Index(name = "idx_security_audit_occurred_at", columnList = "occurredAt"),
        @Index(name = "idx_security_audit_actor", columnList = "actor")
})
public class SecurityAuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String actor;

    @Column(nullable = false, length = 12)
    private String method;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(nullable = false)
    private int statusCode;

    @Column(nullable = false, length = 80)
    private String correlationId;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected SecurityAuditEvent() {}

    public SecurityAuditEvent(String actor, String method, String path, int statusCode,
                              String correlationId, Instant occurredAt) {
        this.actor = actor;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.correlationId = correlationId;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public String getActor() { return actor; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public int getStatusCode() { return statusCode; }
    public String getCorrelationId() { return correlationId; }
    public Instant getOccurredAt() { return occurredAt; }
}
