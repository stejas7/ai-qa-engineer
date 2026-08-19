package com.aiqa.testmanagement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Repository for M13 test management and traceability records. */
public interface TestTraceabilityRepository extends JpaRepository<TestTraceability, UUID> {
    List<TestTraceability> findByCompanyIdAndProductIdOrderByCreatedAtDesc(UUID companyId, UUID productId);
    boolean existsByCompanyIdAndProductIdAndTestCaseIdIgnoreCase(UUID companyId, UUID productId, String testCaseId);
}
