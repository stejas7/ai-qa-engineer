package com.aiqa.healing;

/**
 * Categories used by Auravis M6 to separate automation/environment failures from genuine business failures.
 * @author Tejas Shah
 */
public enum FailureCategory {
    LOCATOR_FAILURE(true),
    TIMEOUT(true),
    NAVIGATION_FAILURE(true),
    TRANSIENT_BROWSER_FAILURE(true),
    ASSERTION_FAILURE(false),
    BUSINESS_FAILURE(false),
    UNSUPPORTED_ACTION(false),
    UNKNOWN(false);

    private final boolean recoverable;

    FailureCategory(boolean recoverable) {
        this.recoverable = recoverable;
    }

    public boolean isRecoverable() {
        return recoverable;
    }
}
