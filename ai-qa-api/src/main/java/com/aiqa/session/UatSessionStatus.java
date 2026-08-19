package com.aiqa.session;

/** Lifecycle state for an M10 UAT session. */
public enum UatSessionStatus {
    CREATED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
