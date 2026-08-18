package com.aiqa.healing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Tests conservative M6 healing authorization. @author Tejas Shah */
@ExtendWith(MockitoExtension.class)
class HealingDecisionServiceTest {
    @Mock HealingAttemptRepository attempts;

    @Test
    void allowsHighConfidenceRecoverableFailure() {
        when(attempts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        HealingDecisionService service = new HealingDecisionService(new FailureClassifier(), attempts);
        var decision = service.evaluate("TC-1", "locator element not found", "semantic fallback", 0.95);
        assertEquals("AUTO_HEAL_ALLOWED", decision.decision());
        assertTrue(decision.recoverable());
        assertEquals(FailureCategory.LOCATOR_FAILURE, decision.category());
        verify(attempts).save(any());
    }

    @Test
    void blocksProtectedFailureEvenAtHighConfidence() {
        when(attempts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        HealingDecisionService service = new HealingDecisionService(new FailureClassifier(), attempts);
        var decision = service.evaluate("TC-2", "assertion expected Welcome", "change assertion", 0.99);
        assertEquals("NO_AUTO_HEAL", decision.decision());
        assertFalse(decision.recoverable());
    }

    @Test
    void blocksRecoverableFailureBelowThreshold() {
        when(attempts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        HealingDecisionService service = new HealingDecisionService(new FailureClassifier(), attempts);
        var decision = service.evaluate("TC-3", "timeout exceeded", "retry", 0.70);
        assertEquals("NO_AUTO_HEAL", decision.decision());
        assertEquals(0.70, decision.confidence());
    }
}
