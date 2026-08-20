package com.aiqa.application;

import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** M16 tenant-scoped product/environment registry. Company identity is derived from the authenticated user. */
@Service
public class ProductRegistryService {
    private final ApplicationTargetRepository targets;
    private final AppUserRepository users;

    public ProductRegistryService(ApplicationTargetRepository targets, AppUserRepository users) {
        this.targets = targets;
        this.users = users;
    }

    public List<ApplicationTarget> list(String actorEmail, boolean activeOnly) {
        AppUser actor = requireActiveUser(actorEmail);
        return activeOnly
                ? targets.findByCompanyIdAndActiveTrueOrderByCreatedAtDesc(actor.getCompanyId())
                : targets.findByCompanyIdOrderByCreatedAtDesc(actor.getCompanyId());
    }

    public ApplicationTarget create(String actorEmail, CreateProductEnvironmentRequest request) {
        AppUser actor = requireManager(actorEmail);
        if (request == null) throw new IllegalArgumentException("product request is required");
        String name = required(request.name(), "name");
        String environment = normalizeEnvironment(request.environment());
        String baseUrl = validateBaseUrl(request.baseUrl());
        String authType = request.authType() == null || request.authType().isBlank()
                ? "NONE" : request.authType().trim().toUpperCase(Locale.ROOT);
        if (targets.existsByCompanyIdAndNameIgnoreCaseAndEnvironmentIgnoreCase(actor.getCompanyId(), name, environment)) {
            throw new IllegalStateException("Product environment is already registered for this company");
        }
        return targets.save(new ApplicationTarget(name, baseUrl, environment, authType, actor.getCompanyId()));
    }

    public ApplicationTarget setActive(String actorEmail, UUID targetId, boolean active) {
        AppUser actor = requireManager(actorEmail);
        if (targetId == null) throw new IllegalArgumentException("targetId is required");
        ApplicationTarget target = targets.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Product environment not found"));
        if (!actor.getCompanyId().equals(target.getCompanyId())) {
            throw new SecurityException("Cross-tenant product access denied");
        }
        target.setActive(active);
        return targets.save(target);
    }

    private AppUser requireManager(String email) {
        AppUser actor = requireActiveUser(email);
        if (actor.getRole() != UserRole.COMPANY_ADMIN && actor.getRole() != UserRole.QA_MANAGER) {
            throw new SecurityException("Company admin or QA manager role required");
        }
        return actor;
    }

    private AppUser requireActiveUser(String email) {
        if (email == null || email.isBlank()) throw new SecurityException("Authentication required");
        AppUser actor = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
        if (!actor.isActive()) throw new IllegalStateException("User is inactive");
        return actor;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private String normalizeEnvironment(String environment) {
        return environment == null || environment.isBlank() ? "UAT" : environment.trim().toUpperCase(Locale.ROOT);
    }

    private String validateBaseUrl(String raw) {
        String value = required(raw, "baseUrl");
        URI uri;
        try { uri = URI.create(value); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("baseUrl must be a valid absolute URL"); }
        if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException("baseUrl must be an absolute http/https URL");
        }
        return uri.toString();
    }

    public record CreateProductEnvironmentRequest(String name, String baseUrl, String environment, String authType) {}
}
