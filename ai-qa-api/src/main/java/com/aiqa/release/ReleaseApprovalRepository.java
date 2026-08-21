package com.aiqa.release;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReleaseApprovalRepository extends JpaRepository<ReleaseApproval, UUID> {
    List<ReleaseApproval> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
    boolean existsByRunId(UUID runId);
}
