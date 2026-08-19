package com.aiqa.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UatSessionRepository extends JpaRepository<UatSession, UUID> {
    List<UatSession> findAllByOrderByCreatedAtDesc();
    List<UatSession> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
    List<UatSession> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
    List<UatSession> findByCompanyIdAndApplicationIdOrderByCreatedAtDesc(UUID companyId, UUID applicationId);
}
