package com.aiqa.performance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Repository for M11 performance evidence. */
public interface LoadTestRunRepository extends JpaRepository<LoadTestRun, UUID> {
    List<LoadTestRun> findTop50ByOrderByCreatedAtDesc();
}
