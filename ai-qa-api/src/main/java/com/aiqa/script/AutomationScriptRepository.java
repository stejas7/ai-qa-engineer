package com.aiqa.script;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutomationScriptRepository extends JpaRepository<AutomationScript, UUID> {
    List<AutomationScript> findByCompanyIdAndProductIdOrderByCreatedAtDesc(UUID companyId, UUID productId);
    boolean existsByCompanyIdAndProductIdAndNameIgnoreCase(UUID companyId, UUID productId, String name);
}
