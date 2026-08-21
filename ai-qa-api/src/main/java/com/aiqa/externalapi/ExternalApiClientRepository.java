package com.aiqa.externalapi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * External API client repository with an explicit class name that cannot collide
 * with the security package repository during Spring Data bean registration.
 */
public interface ExternalApiClientRepository extends JpaRepository<ApiClient, UUID> {
    Optional<ApiClient> findByClientId(String clientId);
    List<ApiClient> findByCompanyIdOrderByCreatedAtAsc(UUID companyId);
}
