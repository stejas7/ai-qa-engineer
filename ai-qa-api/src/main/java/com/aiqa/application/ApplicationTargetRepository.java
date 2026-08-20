package com.aiqa.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationTargetRepository extends JpaRepository<ApplicationTarget, UUID> {
    List<ApplicationTarget> findAllByOrderByCreatedAtDesc();
    List<ApplicationTarget> findByActiveTrueOrderByCreatedAtDesc();
    List<ApplicationTarget> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
    List<ApplicationTarget> findByCompanyIdAndActiveTrueOrderByCreatedAtDesc(UUID companyId);
    boolean existsByCompanyIdAndNameIgnoreCase(UUID companyId, String name);
    boolean existsByCompanyIdAndNameIgnoreCaseAndEnvironmentIgnoreCase(UUID companyId, String name, String environment);
}
