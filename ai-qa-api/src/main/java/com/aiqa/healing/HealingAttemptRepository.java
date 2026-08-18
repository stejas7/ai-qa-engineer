package com.aiqa.healing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Repository for M6 self-healing audit history. @author Tejas Shah */
public interface HealingAttemptRepository extends JpaRepository<HealingAttempt, UUID> {
    List<HealingAttempt> findTop100ByOrderByCreatedAtDesc();
    long countByDecisionIgnoreCase(String decision);
}
