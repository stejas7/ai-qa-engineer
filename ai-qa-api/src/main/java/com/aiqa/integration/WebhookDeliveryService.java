package com.aiqa.integration;

import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Sends bounded outbound webhook calls and persists delivery metadata only. */
@Service
public class WebhookDeliveryService {
    private final IntegrationEndpointService integrations;
    private final WebhookDeliveryRepository deliveries;
    private final AppUserRepository users;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public WebhookDeliveryService(IntegrationEndpointService integrations,
                                  WebhookDeliveryRepository deliveries,
                                  AppUserRepository users) {
        this.integrations = integrations;
        this.deliveries = deliveries;
        this.users = users;
    }

    public WebhookDelivery sendTest(String actorEmail, UUID endpointId) {
        IntegrationEndpoint endpoint = integrations.requireSameTenant(actorEmail, endpointId);
        if (!endpoint.isActive()) throw new IllegalStateException("Integration is inactive");
        String body = "{\"eventType\":\"UAT_COMPLETED\",\"source\":\"AI UAT Engineer\",\"test\":true}";
        return deliver(endpoint, "UAT_COMPLETED", body);
    }

    public List<WebhookDelivery> history(String actorEmail) {
        AppUser actor = requireUser(actorEmail);
        return deliveries.findTop100ByCompanyIdOrderByCreatedAtDesc(actor.getCompanyId());
    }

    private WebhookDelivery deliver(IntegrationEndpoint endpoint, String eventType, String body) {
        int status = 0;
        boolean success = false;
        String message;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint.getUrl()))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "AI-UAT-Engineer-Webhook/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            status = response.statusCode();
            success = status >= 200 && status < 300;
            message = success ? "Delivered" : "Remote endpoint returned HTTP " + status;
        } catch (Exception e) {
            message = "Delivery failed: " + e.getClass().getSimpleName();
        }
        return deliveries.save(new WebhookDelivery(endpoint.getId(), endpoint.getCompanyId(), eventType, status, success, message));
    }

    private AppUser requireUser(String email) {
        if (email == null || email.isBlank()) throw new SecurityException("Authentication required");
        AppUser actor = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
        if (!actor.isActive()) throw new SecurityException("User is inactive");
        return actor;
    }
}
