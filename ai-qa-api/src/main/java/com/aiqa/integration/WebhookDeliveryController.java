package com.aiqa.integration;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations")
public class WebhookDeliveryController {
    private final WebhookDeliveryService deliveries;

    public WebhookDeliveryController(WebhookDeliveryService deliveries) {
        this.deliveries = deliveries;
    }

    @PostMapping("/{id}/test")
    public DeliveryView test(Authentication authentication, @PathVariable UUID id) {
        return DeliveryView.from(deliveries.sendTest(authentication.getName(), id));
    }

    @GetMapping("/deliveries")
    public List<DeliveryView> history(Authentication authentication) {
        return deliveries.history(authentication.getName()).stream().map(DeliveryView::from).toList();
    }

    public record DeliveryView(UUID id, UUID endpointId, String eventType, int statusCode,
                               boolean success, String message, String createdAt) {
        static DeliveryView from(WebhookDelivery delivery) {
            return new DeliveryView(delivery.getId(), delivery.getEndpointId(), delivery.getEventType(),
                    delivery.getStatusCode(), delivery.isSuccess(), delivery.getMessage(), delivery.getCreatedAt().toString());
        }
    }
}
