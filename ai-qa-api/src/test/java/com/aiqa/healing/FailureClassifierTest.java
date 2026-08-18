package com.aiqa.healing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the deterministic M6 healing classifier. @author Tejas Shah */
class FailureClassifierTest {
    private final FailureClassifier classifier = new FailureClassifier();

    @Test
    void classifiesRecoverableAutomationFailures() {
        assertEquals(FailureCategory.LOCATOR_FAILURE, classifier.classify("locator did not match element"));
        assertEquals(FailureCategory.TIMEOUT, classifier.classify("Timeout 30000ms exceeded"));
        assertEquals(FailureCategory.NAVIGATION_FAILURE, classifier.classify("net::ERR_CONNECTION_REFUSED"));
        assertTrue(classifier.classify("locator not found").isRecoverable());
    }

    @Test
    void protectsBusinessAndAssertionFailures() {
        assertEquals(FailureCategory.ASSERTION_FAILURE, classifier.classify("expected Welcome but assertion failed"));
        assertEquals(FailureCategory.BUSINESS_FAILURE, classifier.classify("business validation rule failed"));
        assertEquals(FailureCategory.UNSUPPORTED_ACTION, classifier.classify("Unsupported automation step: drag"));
        assertFalse(classifier.classify("assertion failed").isRecoverable());
    }
}
