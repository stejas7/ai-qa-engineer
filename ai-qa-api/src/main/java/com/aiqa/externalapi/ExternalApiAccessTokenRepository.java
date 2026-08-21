package com.aiqa.externalapi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for external API client access tokens.
 * Uses a distinct interface name to avoid a Spring Data bean-name collision
 * with com.aiqa.security.ApiAccessTokenRepository.
 *
 * @author Tejas Shah
 */
public interface ExternalApiAccessTokenRepository extends JpaRepository<ApiAccessToken, UUID> {
    Optional<ApiAccessToken> findByTokenHash(String tokenHash);
    List<ApiAccessToken> findByApiClientIdAndRevokedFalse(UUID apiClientId);
}
