package com.aiqa.analytics;

import jakarta.persistence.*;
import java.time.Instant;

/** Stores an anonymous page visit without persisting an IP address. */
@Entity
@Table(name = "site_visits", indexes = {
        @Index(name = "idx_site_visits_visited_at", columnList = "visitedAt"),
        @Index(name = "idx_site_visits_visitor_id", columnList = "visitorId")
})
public class SiteVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String path;

    @Column(nullable = false, length = 64)
    private String visitorId;

    @Column(nullable = false)
    private Instant visitedAt;

    protected SiteVisit() {}

    public SiteVisit(String path, String visitorId, Instant visitedAt) {
        this.path = path;
        this.visitorId = visitorId;
        this.visitedAt = visitedAt;
    }

    public Long getId() { return id; }
    public String getPath() { return path; }
    public String getVisitorId() { return visitorId; }
    public Instant getVisitedAt() { return visitedAt; }
}
