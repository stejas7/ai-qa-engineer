package com.aiqa.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface SiteVisitRepository extends JpaRepository<SiteVisit, Long> {
    List<SiteVisit> findAllByVisitedAtAfterOrderByVisitedAtAsc(Instant after);
}
