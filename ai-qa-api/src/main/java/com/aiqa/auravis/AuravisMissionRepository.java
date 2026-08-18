package com.aiqa.auravis;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuravisMissionRepository extends JpaRepository<AuravisMission, UUID> {}
