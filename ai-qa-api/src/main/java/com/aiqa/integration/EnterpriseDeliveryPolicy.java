package com.aiqa.integration;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/** M38 bounded retry, failure classification and deterministic idempotency policy. */
@Service
public class EnterpriseDeliveryPolicy {
    private static final List<Duration> BACKOFF = List.of(Duration.ofSeconds(2), Duration.ofSeconds(10), Duration.ofSeconds(30));

    public DeliveryDecision decide(int attempt, Integer statusCode) {
        FailureClass failure = classify(statusCode);
        boolean retry = failure == FailureClass.TRANSIENT && attempt >= 1 && attempt <= BACKOFF.size();
        Duration delay = retry ? BACKOFF.get(attempt - 1) : Duration.ZERO;
        return new DeliveryDecision(retry, delay.toSeconds(), failure.name(), retry ? "RETRY_SCHEDULED" : "FINAL");
    }

    public String idempotencyKey(String companyId, String provider, String eventType, String eventId) {
        String canonical = required(companyId) + ":" + required(provider).toUpperCase(Locale.ROOT) + ":" + required(eventType).toUpperCase(Locale.ROOT) + ":" + required(eventId);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private FailureClass classify(Integer statusCode) {
        if (statusCode == null || statusCode == 408 || statusCode == 429 || statusCode >= 500) return FailureClass.TRANSIENT;
        if (statusCode >= 200 && statusCode < 300) return FailureClass.NONE;
        return FailureClass.PERMANENT;
    }

    private String required(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("idempotency components are required");
        return value.trim();
    }

    enum FailureClass { NONE, TRANSIENT, PERMANENT }
    public record DeliveryDecision(boolean retry, long retryAfterSeconds, String failureClass, String state) {}
}
