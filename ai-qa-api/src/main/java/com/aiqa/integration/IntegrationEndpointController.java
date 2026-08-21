package com.aiqa.integration;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationEndpointController {
    private final IntegrationEndpointService integrations;

    public IntegrationEndpointController(IntegrationEndpointService integrations) {
        this.integrations = integrations;
    }

    @GetMapping
    public List<IntegrationView> list(Authentication authentication) {
        return integrations.list(authentication.getName()).stream().map(IntegrationView::from).toList();
    }

    @PostMapping
    public IntegrationView create(Authentication authentication, @RequestBody CreateIntegrationRequest request) {
        if (request == null) throw new IllegalArgumentException("integration request is required");
        return IntegrationView.from(integrations.create(authentication.getName(), request.name(), request.url(), request.eventTypes()));
    }

    @PatchMapping("/{id}/active")
    public IntegrationView active(Authentication authentication, @PathVariable UUID id, @RequestBody ActiveRequest request) {
        if (request == null) throw new IllegalArgumentException("active request is required");
        return IntegrationView.from(integrations.setActive(authentication.getName(), id, request.active()));
    }

    public record CreateIntegrationRequest(String name, String url, List<String> eventTypes) {}
    public record ActiveRequest(boolean active) {}
    public record IntegrationView(UUID id, UUID companyId, String name, String url, List<String> eventTypes,
                                  boolean active, String createdAt) {
        static IntegrationView from(IntegrationEndpoint endpoint) {
            List<String> events = endpoint.getEventTypes().isBlank() ? List.of() : Arrays.asList(endpoint.getEventTypes().split(","));
            return new IntegrationView(endpoint.getId(), endpoint.getCompanyId(), endpoint.getName(), endpoint.getUrl(),
                    events, endpoint.isActive(), endpoint.getCreatedAt().toString());
        }
    }
}
