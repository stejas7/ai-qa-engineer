package com.aiqa.credential;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository for tenant-bound credential profile metadata. */
public interface CredentialProfileRepository extends JpaRepository<CredentialProfile, UUID> {
    Optional<CredentialProfile> findByApplicationTargetId(UUID applicationTargetId);
    List<CredentialProfile> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
