package com.aiqa.testmanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** M13 ISTQB-aligned traceability from requirement and test condition through execution and completion evidence. */
@Entity
@Table(name = "ai_uat_test_traceability")
public class TestTraceability {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false) private UUID companyId;
    @Column(nullable = false) private UUID productId;
    @Column(nullable = false) private String requirementId;
    @Column(nullable = false, length = 2000) private String testCondition;
    @Column(nullable = false) private String testCaseId;
    private UUID automationScriptId;
    @Column(nullable = false) private String riskLevel;
    @Column(nullable = false, length = 2000) private String expectedResult;
    private String executionId;
    @Column(length = 2000) private String actualResult;
    @Column(nullable = false) private String status = "NOT_RUN";
    private String defectRef;
    @Column(nullable = false) private boolean entryCriteriaMet;
    @Column(nullable = false) private boolean exitCriteriaMet;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant updatedAt = Instant.now();

    protected TestTraceability() {}

    public TestTraceability(UUID companyId, UUID productId, String requirementId, String testCondition,
                            String testCaseId, UUID automationScriptId, String riskLevel,
                            String expectedResult, boolean entryCriteriaMet) {
        this.companyId = companyId;
        this.productId = productId;
        this.requirementId = requirementId;
        this.testCondition = testCondition;
        this.testCaseId = testCaseId;
        this.automationScriptId = automationScriptId;
        this.riskLevel = riskLevel;
        this.expectedResult = expectedResult;
        this.entryCriteriaMet = entryCriteriaMet;
    }

    public void recordExecution(String executionId, String status, String actualResult, String defectRef) {
        this.executionId = executionId;
        this.status = status;
        this.actualResult = actualResult;
        this.defectRef = defectRef;
        this.exitCriteriaMet = "PASS".equals(status);
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getProductId() { return productId; }
    public String getRequirementId() { return requirementId; }
    public String getTestCondition() { return testCondition; }
    public String getTestCaseId() { return testCaseId; }
    public UUID getAutomationScriptId() { return automationScriptId; }
    public String getRiskLevel() { return riskLevel; }
    public String getExpectedResult() { return expectedResult; }
    public String getExecutionId() { return executionId; }
    public String getActualResult() { return actualResult; }
    public String getStatus() { return status; }
    public String getDefectRef() { return defectRef; }
    public boolean isEntryCriteriaMet() { return entryCriteriaMet; }
    public boolean isExitCriteriaMet() { return exitCriteriaMet; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
