package com.aiqa.healing;

import org.springframework.stereotype.Service;

import java.util.Locale;

/** Deterministic failure classifier used before any self-healing attempt. @author Tejas Shah */
@Service
public class FailureClassifier {

    public FailureCategory classify(String message) {
        String text = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (text.contains("unsupported automation step")) return FailureCategory.UNSUPPORTED_ACTION;
        if (text.contains("strict mode violation") || text.contains("locator") || text.contains("element not found") || text.contains("no node found")) {
            return FailureCategory.LOCATOR_FAILURE;
        }
        if (text.contains("timeout") || text.contains("timed out")) return FailureCategory.TIMEOUT;
        if (text.contains("navigation") || text.contains("net::err") || text.contains("connection refused")) return FailureCategory.NAVIGATION_FAILURE;
        if (text.contains("browser has been closed") || text.contains("target page, context or browser has been closed")) {
            return FailureCategory.TRANSIENT_BROWSER_FAILURE;
        }
        if (text.contains("expected") || text.contains("assert") || text.contains("waiting for getbytext")) {
            return FailureCategory.ASSERTION_FAILURE;
        }
        if (text.contains("business") || text.contains("validation") || text.contains("rule")) return FailureCategory.BUSINESS_FAILURE;
        return FailureCategory.UNKNOWN;
    }
}
