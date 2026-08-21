package com.aiqa.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<AppUser> findByCompanyIdOrderByCreatedAtAsc(UUID companyId);
    long countByCompanyIdAndRoleAndActiveTrue(UUID companyId, UserRole role);
    long countByRoleAndActiveTrue(UserRole role);
}
