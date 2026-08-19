package com.aiqa.testmanagement;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.script.AutomationScript;
import com.aiqa.script.AutomationScriptRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** M13 ISTQB-aligned test management, risk, traceability and completion reporting. */
@Service
public class TestManagementService {
    private final TestTraceabilityRepository traceability;
    private final ApplicationTargetRepository products;
    private final AutomationScriptRepository scripts;

    public TestManagementService(TestTraceabilityRepository traceability,
                                 ApplicationTargetRepository products,
                                 AutomationScriptRepository scripts) {
        this.traceability = traceability;
        this.products = products;
        this.scripts = scripts;
    }

    public TestTraceability create(CreateTraceabilityRequest request) {
        validateCreate(request);
        requireOwnedActiveProduct(request.companyId(), request.productId());
        if (traceability.existsByCompanyIdAndProductIdAndTestCaseIdIgnoreCase(
                request.companyId(), request.productId(), request.testCaseId().trim())) {
            throw new IllegalStateException("testCaseId already exists for product");
        }
        UUID scriptId = request.automationScriptId();
        if (scriptId != null) validateApprovedScript(request.companyId(), request.productId(), scriptId);
        String risk = normalizeRisk(request.riskLevel());
        return traceability.save(new TestTraceability(
                request.companyId(), request.productId(), request.requirementId().trim(),
                request.testCondition().trim(), request.testCaseId().trim(), scriptId, risk,
                request.expectedResult().trim(), request.entryCriteriaMet()));
    }

    public List<TestTraceability> list(UUID companyId, UUID productId) {
        requireOwnedActiveProduct(companyId, productId);
        return traceability.findByCompanyIdAndProductIdOrderByCreatedAtDesc(companyId, productId);
    }

    public TestTraceability recordExecution(UUID id, ExecutionResultRequest request) {
        if (request == null) throw new IllegalArgumentException("execution result is required");
        TestTraceability record = traceability.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("traceability record not found"));
        String status = normalizeStatus(request.status());
        if (!record.isEntryCriteriaMet()) throw new IllegalStateException("entry criteria are not met");
        if ("FAIL".equals(status) && (request.defectRef() == null || request.defectRef().isBlank()))
            throw new IllegalArgumentException("defectRef is required for failed tests");
        if (request.executionId() == null || request.executionId().isBlank())
            throw new IllegalArgumentException("executionId is required");
        if (request.actualResult() == null || request.actualResult().isBlank())
            throw new IllegalArgumentException("actualResult is required");
        record.recordExecution(request.executionId().trim(), status, request.actualResult().trim(),
                request.defectRef() == null ? null : request.defectRef().trim());
        return traceability.save(record);
    }

    public TestCompletionSummary summary(UUID companyId, UUID productId) {
        List<TestTraceability> records = list(companyId, productId);
        long total = records.size();
        long passed = records.stream().filter(r -> "PASS".equals(r.getStatus())).count();
        long failed = records.stream().filter(r -> "FAIL".equals(r.getStatus())).count();
        long notRun = records.stream().filter(r -> "NOT_RUN".equals(r.getStatus())).count();
        long highRisk = records.stream().filter(r -> "HIGH".equals(r.getRiskLevel())).count();
        long failedHighRisk = records.stream().filter(r -> "HIGH".equals(r.getRiskLevel()) && "FAIL".equals(r.getStatus())).count();
        long linkedAutomation = records.stream().filter(r -> r.getAutomationScriptId() != null).count();
        double coverage = total == 0 ? 0 : linkedAutomation * 100.0 / total;
        boolean exitCriteriaMet = total > 0 && notRun == 0 && failedHighRisk == 0 && failed == 0;
        String recommendation = exitCriteriaMet ? "READY_FOR_RELEASE_REVIEW" : "TEST_COMPLETION_BLOCKED";
        return new TestCompletionSummary(total, passed, failed, notRun, highRisk, failedHighRisk,
                linkedAutomation, coverage, exitCriteriaMet, recommendation);
    }

    private void validateCreate(CreateTraceabilityRequest request) {
        if (request == null || request.companyId() == null || request.productId() == null)
            throw new IllegalArgumentException("companyId and productId are required");
        if (request.requirementId() == null || request.requirementId().isBlank())
            throw new IllegalArgumentException("requirementId is required");
        if (request.testCondition() == null || request.testCondition().isBlank())
            throw new IllegalArgumentException("testCondition is required");
        if (request.testCaseId() == null || request.testCaseId().isBlank())
            throw new IllegalArgumentException("testCaseId is required");
        if (request.expectedResult() == null || request.expectedResult().isBlank())
            throw new IllegalArgumentException("expectedResult is required");
    }

    private ApplicationTarget requireOwnedActiveProduct(UUID companyId, UUID productId) {
        if (companyId == null || productId == null) throw new IllegalArgumentException("companyId and productId are required");
        ApplicationTarget product = products.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
        if (!product.isActive()) throw new IllegalStateException("product is inactive");
        if (product.getCompanyId() == null || !product.getCompanyId().equals(companyId))
            throw new IllegalArgumentException("product does not belong to company");
        return product;
    }

    private void validateApprovedScript(UUID companyId, UUID productId, UUID scriptId) {
        AutomationScript script = scripts.findById(scriptId)
                .orElseThrow(() -> new IllegalArgumentException("automation script not found"));
        if (!companyId.equals(script.getCompanyId()) || !productId.equals(script.getProductId()))
            throw new IllegalArgumentException("automation script does not belong to company/product");
        if (!"APPROVED".equals(script.getStatus()))
            throw new IllegalStateException("automation script must be approved before traceability linkage");
    }

    private String normalizeRisk(String value) {
        String risk = value == null || value.isBlank() ? "MEDIUM" : value.trim().toUpperCase(Locale.ROOT);
        if (!List.of("LOW", "MEDIUM", "HIGH").contains(risk))
            throw new IllegalArgumentException("riskLevel must be LOW, MEDIUM or HIGH");
        return risk;
    }

    private String normalizeStatus(String value) {
        String status = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!List.of("PASS", "FAIL", "BLOCKED").contains(status))
            throw new IllegalArgumentException("status must be PASS, FAIL or BLOCKED");
        return status;
    }

    public record CreateTraceabilityRequest(UUID companyId, UUID productId, String requirementId,
                                            String testCondition, String testCaseId, UUID automationScriptId,
                                            String riskLevel, String expectedResult, boolean entryCriteriaMet) {}
    public record ExecutionResultRequest(String executionId, String status, String actualResult, String defectRef) {}
    public record TestCompletionSummary(long total, long passed, long failed, long notRun, long highRisk,
                                        long failedHighRisk, long automatedLinks, double automationCoveragePercent,
                                        boolean exitCriteriaMet, String releaseRecommendation) {}
}
