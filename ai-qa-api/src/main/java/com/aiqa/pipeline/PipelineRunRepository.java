package com.aiqa.pipeline;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, UUID> {
    List<PipelineRun> findAllByOrderByCreatedAtDesc();
    List<PipelineRun> findByCompanyOrderByCreatedAtDesc(String company);
}
