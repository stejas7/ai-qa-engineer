package com.aiqa.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiClientRepository extends JpaRepository<ApiClient, UUID> {
    Optional<ApiClient> findByClientId(String clientId);
    List<ApiClient> findByCompanyIdOrderByCreatedAtAsc(UUID companyId);
}
