package com.aiqa.externalapi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiAccessTokenRepository extends JpaRepository<ApiAccessToken, UUID> {
    Optional<ApiAccessToken> findByTokenHash(String tokenHash);
    List<ApiAccessToken> findByApiClientIdAndRevokedFalse(UUID apiClientId);
}
