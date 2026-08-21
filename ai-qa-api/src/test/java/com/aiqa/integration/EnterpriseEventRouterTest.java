package com.aiqa.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnterpriseEventRouterTest {
    @Test void releaseBlockedRoutesToAllCriticalProviders() {
        var routes = new EnterpriseEventRouter().routesFor("release_blocked");
        assertEquals(4, routes.size());
        assertTrue(routes.stream().anyMatch(r -> r.providerKey().equals("JIRA")));
        assertTrue(routes.stream().anyMatch(r -> r.providerKey().equals("GITHUB")));
    }

    @Test void deliveryPolicyRetriesTransientButNotPermanentFailure() {
        var policy = new EnterpriseDeliveryPolicy();
        var transientFailure = policy.decide(1, 503);
        assertTrue(transientFailure.retry());
        assertEquals(2, transientFailure.retryAfterSeconds());
        var permanentFailure = policy.decide(1, 400);
        assertFalse(permanentFailure.retry());
        assertEquals("PERMANENT", permanentFailure.failureClass());
    }

    @Test void idempotencyKeyIsStable() {
        var policy = new EnterpriseDeliveryPolicy();
        assertEquals(policy.idempotencyKey("tenant", "jira", "uat_failed", "42"), policy.idempotencyKey("tenant", "JIRA", "UAT_FAILED", "42"));
    }
}
