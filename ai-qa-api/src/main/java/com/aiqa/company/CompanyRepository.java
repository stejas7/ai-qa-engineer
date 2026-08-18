package com.aiqa.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Repository for registered company workspaces. */
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findAllByOrderByCreatedAtDesc();
    List<Company> findByActiveTrueOrderByCreatedAtDesc();
    boolean existsBySlugIgnoreCase(String slug);
}
