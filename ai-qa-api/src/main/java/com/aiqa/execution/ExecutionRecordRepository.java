package com.aiqa.execution;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExecutionRecordRepository extends JpaRepository<ExecutionRecord, UUID> {
    List<ExecutionRecord> findTop100ByOrderByExecutedAtDesc();
    long countByStatusIgnoreCase(String status);
}
