package com.aiqa.analytics;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Stores privacy-conscious page visits. Raw IP addresses and session identifiers are never persisted. */
@Entity
@Table(name = "site_visits", indexes = {
        @Index(name = "idx_site_visits_visited_at", columnList = "visitedAt"),
        @Index(name = "idx_site_visits_visitor_id", columnList = "visitorId"),
        @Index(name = "idx_site_visits_user_email", columnList = "userEmail")
})
public class SiteVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String path;

    @Column(nullable = false, length = 64)
    private String visitorId;

    /** Populated only when the visit belongs to an authenticated application user. */
    @Column(length = 320)
    private String userEmail;

    /** Tenant scope for authenticated users; null for public/anonymous visitors. */
    private UUID companyId;

    @Column(nullable = false)
    private Instant visitedAt;

    protected SiteVisit() {}

    public SiteVisit(String path, String visitorId, String userEmail, UUID companyId, Instant visitedAt) {
        this.path = path;
        this.visitorId = visitorId;
        this.userEmail = userEmail;
        this.companyId = companyId;
        this.visitedAt = visitedAt;
    }

    public Long getId() { return id; }
    public String getPath() { return path; }
    public String getVisitorId() { return visitorId; }
    public String getUserEmail() { return userEmail; }
    public UUID getCompanyId() { return companyId; }
    public Instant getVisitedAt() { return visitedAt; }
}
