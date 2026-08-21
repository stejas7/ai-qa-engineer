package com.aiqa.integration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IntegrationEndpointRepository extends JpaRepository<IntegrationEndpoint, UUID> {
    List<IntegrationEndpoint> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
