package com.aiqa.governance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Platform-owner read-only audit trail. */
@RestController
@RequestMapping("/api/platform/security-audit")
public class SecurityAuditController {
    private final SecurityAuditEventRepository events;

    public SecurityAuditController(SecurityAuditEventRepository events) {
        this.events = events;
    }

    @GetMapping
    public List<AuditView> recent() {
        return events.findTop200ByOrderByOccurredAtDesc().stream()
                .map(e -> new AuditView(e.getId(), e.getActor(), e.getMethod(), e.getPath(),
                        e.getStatusCode(), e.getCorrelationId(), e.getOccurredAt().toString()))
                .toList();
    }

    public record AuditView(Long id, String actor, String method, String path,
                            int statusCode, String correlationId, String occurredAt) {}
}
