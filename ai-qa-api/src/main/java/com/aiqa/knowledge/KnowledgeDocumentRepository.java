package com.aiqa.knowledge;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/** Persistence for Scorpion knowledge documents. */
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {
}
