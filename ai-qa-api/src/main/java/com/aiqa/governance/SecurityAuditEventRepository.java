package com.aiqa.governance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, Long> {
    List<SecurityAuditEvent> findTop200ByOrderByOccurredAtDesc();
}
