package com.aiqa.session;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for deterministic M10 UAT session lifecycle rules. */
class UatSessionLifecycleTest {

    @Test
    void runsAndCompletesSessionInOrder() {
        UatSession session = new UatSession(UUID.randomUUID(), UUID.randomUUID(), "3.0.0", "Release UAT");

        session.transitionTo(UatSessionStatus.RUNNING);
        assertEquals(UatSessionStatus.RUNNING, session.getStatus());
        assertNotNull(session.getStartedAt());
        assertNull(session.getFinishedAt());

        session.transitionTo(UatSessionStatus.COMPLETED);
        assertEquals(UatSessionStatus.COMPLETED, session.getStatus());
        assertNotNull(session.getFinishedAt());
    }

    @Test
    void rejectsSkippingRunningState() {
        UatSession session = new UatSession(UUID.randomUUID(), UUID.randomUUID(), "3.0.0", "Release UAT");

        assertThrows(IllegalStateException.class,
                () -> session.transitionTo(UatSessionStatus.COMPLETED));
        assertEquals(UatSessionStatus.CREATED, session.getStatus());
    }

    @Test
    void terminalSessionCannotBeReopened() {
        UatSession session = new UatSession(UUID.randomUUID(), UUID.randomUUID(), "3.0.0", "Release UAT");
        session.transitionTo(UatSessionStatus.RUNNING);
        session.transitionTo(UatSessionStatus.FAILED);

        assertThrows(IllegalStateException.class,
                () -> session.transitionTo(UatSessionStatus.RUNNING));
        assertEquals(UatSessionStatus.FAILED, session.getStatus());
    }

    @Test
    void repeatingCurrentStateIsIdempotent() {
        UatSession session = new UatSession(UUID.randomUUID(), UUID.randomUUID(), "3.0.0", "Release UAT");

        session.transitionTo(UatSessionStatus.CREATED);
        assertEquals(UatSessionStatus.CREATED, session.getStatus());
    }
}
