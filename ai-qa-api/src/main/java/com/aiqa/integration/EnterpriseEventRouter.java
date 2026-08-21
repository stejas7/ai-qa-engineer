package com.aiqa.integration;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * M37 deterministic routing from AI-UAT domain events to enterprise provider actions.
 * Routing is metadata-only; provider credentials remain in the server-side credential registry.
 *
 * @author Tejas Shah
 */
@Service
public class EnterpriseEventRouter {
    private static final Map<String, List<Route>> ROUTES = Map.of(
            "UAT_FAILED", List.of(new Route("JIRA", "CREATE_ISSUE"), new Route("SLACK", "POST_MESSAGE"), new Route("MICROSOFT_TEAMS", "POST_MESSAGE")),
            "UAT_COMPLETED", List.of(new Route("GITHUB", "CREATE_CHECK"), new Route("SLACK", "POST_MESSAGE")),
            "RELEASE_READY", List.of(new Route("GITHUB", "COMMENT_PULL_REQUEST"), new Route("MICROSOFT_TEAMS", "POST_MESSAGE")),
            "RELEASE_BLOCKED", List.of(new Route("JIRA", "CREATE_ISSUE"), new Route("GITHUB", "CREATE_CHECK"), new Route("SLACK", "POST_MESSAGE"), new Route("MICROSOFT_TEAMS", "POST_MESSAGE"))
    );

    public List<Route> routesFor(String eventType) {
        if (eventType == null || eventType.isBlank()) throw new IllegalArgumentException("eventType is required");
        String normalized = eventType.trim().toUpperCase(Locale.ROOT);
        List<Route> routes = ROUTES.get(normalized);
        if (routes == null) throw new IllegalArgumentException("Unsupported enterprise event type");
        return routes;
    }

    public record Route(String providerKey, String action) {}
}
