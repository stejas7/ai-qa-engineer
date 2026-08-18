package com.aiqa.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Company workspace that owns one or more Auravis product targets.
 *
 * <p>Copyright (c) 2026 Tejas Shah.</p>
 */
@Entity
@Table(name = "auravis_companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Company() {
    }

    public Company(String name, String slug) {
        this.name = name.trim();
        this.slug = normalizeSlug(slug == null || slug.isBlank() ? name : slug);
    }

    static String normalizeSlug(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Company slug must contain letters or numbers");
        }
        return normalized;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setActive(boolean active) { this.active = active; }
}
