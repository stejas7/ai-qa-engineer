package com.aiqa.governance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantGovernancePolicyRepository extends JpaRepository<TenantGovernancePolicy, UUID> {}
