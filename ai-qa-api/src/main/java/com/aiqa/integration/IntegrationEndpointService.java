package com.aiqa.integration;

import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Tenant-safe integration endpoint management. */
@Service
public class IntegrationEndpointService {
    private static final Set<String> ALLOWED_EVENTS = Set.of("UAT_COMPLETED", "UAT_FAILED", "RELEASE_READY", "RELEASE_BLOCKED");
    private final IntegrationEndpointRepository endpoints;
    private final AppUserRepository users;

    public IntegrationEndpointService(IntegrationEndpointRepository endpoints, AppUserRepository users) {
        this.endpoints = endpoints;
        this.users = users;
    }

    public List<IntegrationEndpoint> list(String actorEmail) {
        AppUser actor = requireUser(actorEmail);
        return endpoints.findByCompanyIdOrderByCreatedAtDesc(actor.getCompanyId());
    }

    public IntegrationEndpoint create(String actorEmail, String name, String url, List<String> eventTypes) {
        AppUser actor = requireManager(actorEmail);
        String cleanName = required(name, "name");
        String cleanUrl = validateUrl(url);
        String events = normalizeEvents(eventTypes);
        return endpoints.save(new IntegrationEndpoint(actor.getCompanyId(), cleanName, cleanUrl, events));
    }

    public IntegrationEndpoint setActive(String actorEmail, UUID id, boolean active) {
        AppUser actor = requireManager(actorEmail);
        IntegrationEndpoint endpoint = requireSameTenant(actor, id);
        endpoint.setActive(active);
        return endpoints.save(endpoint);
    }

    public IntegrationEndpoint requireSameTenant(String actorEmail, UUID id) {
        return requireSameTenant(requireUser(actorEmail), id);
    }

    private IntegrationEndpoint requireSameTenant(AppUser actor, UUID id) {
        if (id == null) throw new IllegalArgumentException("integration id is required");
        IntegrationEndpoint endpoint = endpoints.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        if (!actor.getCompanyId().equals(endpoint.getCompanyId())) throw new SecurityException("Cross-tenant integration access denied");
        return endpoint;
    }

    private AppUser requireManager(String email) {
        AppUser actor = requireUser(email);
        if (actor.getRole() != UserRole.COMPANY_ADMIN && actor.getRole() != UserRole.QA_MANAGER) {
            throw new SecurityException("Company admin or QA manager role required");
        }
        return actor;
    }

    private AppUser requireUser(String email) {
        if (email == null || email.isBlank()) throw new SecurityException("Authentication required");
        AppUser actor = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
        if (!actor.isActive()) throw new SecurityException("User is inactive");
        return actor;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private String normalizeEvents(List<String> eventTypes) {
        if (eventTypes == null || eventTypes.isEmpty()) throw new IllegalArgumentException("At least one event type is required");
        List<String> normalized = eventTypes.stream().map(v -> v == null ? "" : v.trim().toUpperCase(Locale.ROOT)).distinct().toList();
        if (normalized.stream().anyMatch(v -> !ALLOWED_EVENTS.contains(v))) throw new IllegalArgumentException("Unsupported integration event type");
        return String.join(",", normalized);
    }

    private String validateUrl(String raw) {
        String value = required(raw, "url");
        URI uri;
        try { uri = URI.create(value); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Integration URL is invalid"); }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalArgumentException("Integration URL must use HTTPS");
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if ("localhost".equals(host) || host.endsWith(".local")) throw new IllegalArgumentException("Private integration host is not allowed");
        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
                throw new IllegalArgumentException("Private integration host is not allowed");
            }
        } catch (java.net.UnknownHostException e) {
            throw new IllegalArgumentException("Integration host cannot be resolved");
        }
        return uri.toString();
    }
}
