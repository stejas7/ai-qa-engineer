package com.aiqa.testmanagement;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.script.AutomationScript;
import com.aiqa.script.AutomationScriptRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestManagementServiceTest {
    private final TestTraceabilityRepository traceability = mock(TestTraceabilityRepository.class);
    private final ApplicationTargetRepository products = mock(ApplicationTargetRepository.class);
    private final AutomationScriptRepository scripts = mock(AutomationScriptRepository.class);
    private final TestManagementService service = new TestManagementService(traceability, products, scripts);

    @Test void createsRiskBasedTraceabilityWithApprovedAutomation() {
        UUID companyId = UUID.randomUUID(), productId = UUID.randomUUID(), scriptId = UUID.randomUUID();
        when(products.findById(productId)).thenReturn(Optional.of(new ApplicationTarget("Checkout", "https://example.test", "UAT", "NONE", companyId)));
        AutomationScript script = new AutomationScript(companyId, productId, "Checkout", List.of("open the application"));
        script.approve();
        when(scripts.findById(scriptId)).thenReturn(Optional.of(script));
        when(traceability.existsByCompanyIdAndProductIdAndTestCaseIdIgnoreCase(companyId, productId, "TC-001")).thenReturn(false);
        when(traceability.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TestTraceability result = service.create(new TestManagementService.CreateTraceabilityRequest(
                companyId, productId, "REQ-001", "User can checkout", "TC-001", scriptId,
                "high", "Order confirmation is shown", true));

        assertEquals("HIGH", result.getRiskLevel());
        assertEquals("NOT_RUN", result.getStatus());
        assertEquals(scriptId, result.getAutomationScriptId());
    }

    @Test void rejectsUnapprovedAutomationLink() {
        UUID companyId = UUID.randomUUID(), productId = UUID.randomUUID(), scriptId = UUID.randomUUID();
        when(products.findById(productId)).thenReturn(Optional.of(new ApplicationTarget("App", "https://example.test", "UAT", "NONE", companyId)));
        when(scripts.findById(scriptId)).thenReturn(Optional.of(new AutomationScript(companyId, productId, "Draft", List.of("open the application"))));
        assertThrows(IllegalStateException.class, () -> service.create(new TestManagementService.CreateTraceabilityRequest(
                companyId, productId, "REQ-1", "Condition", "TC-1", scriptId, "MEDIUM", "Expected", true)));
    }

    @Test void failedExecutionRequiresDefectReference() {
        UUID id = UUID.randomUUID();
        TestTraceability record = new TestTraceability(UUID.randomUUID(), UUID.randomUUID(), "REQ", "Condition", "TC", null, "HIGH", "Expected", true);
        when(traceability.findById(id)).thenReturn(Optional.of(record));
        assertThrows(IllegalArgumentException.class, () -> service.recordExecution(id,
                new TestManagementService.ExecutionResultRequest("RUN-1", "FAIL", "Actual differs", null)));
    }

    @Test void passMarksExitCriteriaAndCompletionReady() {
        UUID companyId = UUID.randomUUID(), productId = UUID.randomUUID(), id = UUID.randomUUID();
        when(products.findById(productId)).thenReturn(Optional.of(new ApplicationTarget("App", "https://example.test", "UAT", "NONE", companyId)));
        TestTraceability record = new TestTraceability(companyId, productId, "REQ", "Condition", "TC", UUID.randomUUID(), "HIGH", "Expected", true);
        when(traceability.findById(id)).thenReturn(Optional.of(record));
        when(traceability.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service.recordExecution(id, new TestManagementService.ExecutionResultRequest("RUN-1", "PASS", "Expected observed", null));
        when(traceability.findByCompanyIdAndProductIdOrderByCreatedAtDesc(companyId, productId)).thenReturn(List.of(record));

        var summary = service.summary(companyId, productId);
        assertTrue(record.isExitCriteriaMet());
        assertTrue(summary.exitCriteriaMet());
        assertEquals("READY_FOR_RELEASE_REVIEW", summary.releaseRecommendation());
        assertEquals(100.0, summary.automationCoveragePercent());
    }
}
