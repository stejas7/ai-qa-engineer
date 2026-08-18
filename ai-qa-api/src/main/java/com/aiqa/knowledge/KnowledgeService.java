package com.aiqa.knowledge;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Retrieval service used by Scorpion before autonomous reasoning stages.
 * This first production-safe baseline uses deterministic lexical ranking.
 * A vector/embedding retriever can replace the ranking strategy without
 * changing mission orchestration or the REST contract.
 */
@Service
public class KnowledgeService {
    private final KnowledgeDocumentRepository repository;

    public KnowledgeService(KnowledgeDocumentRepository repository) {
        this.repository = repository;
    }

    public KnowledgeDocument add(String title, String type, String content, String sourceReference) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content is required");
        String normalizedType = type == null || type.isBlank() ? "DOCUMENT" : type.trim().toUpperCase(Locale.ROOT);
        return repository.save(new KnowledgeDocument(title.trim(), normalizedType, content.trim(), sourceReference));
    }

    public List<KnowledgeDocument> list() {
        return repository.findAll();
    }

    public List<KnowledgeHit> search(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();
        int safeLimit = Math.max(1, Math.min(limit, 10));
        Set<String> queryTerms = tokens(query);

        return repository.findAll().stream()
                .map(document -> new KnowledgeHit(document, score(document, queryTerms)))
                .filter(hit -> hit.score() > 0)
                .sorted(Comparator.comparingInt(KnowledgeHit::score).reversed())
                .limit(safeLimit)
                .collect(Collectors.toList());
    }

    public String buildContext(String query, int limit) {
        List<KnowledgeHit> hits = search(query, limit);
        if (hits.isEmpty()) return "";
        StringBuilder context = new StringBuilder("Relevant project knowledge:\n");
        for (KnowledgeHit hit : hits) {
            KnowledgeDocument d = hit.document();
            context.append("- [").append(d.getType()).append("] ")
                    .append(d.getTitle()).append(": ")
                    .append(truncate(d.getContent(), 900));
            if (d.getSourceReference() != null && !d.getSourceReference().isBlank()) {
                context.append(" (source: ").append(d.getSourceReference()).append(')');
            }
            context.append('\n');
        }
        return context.toString();
    }

    private int score(KnowledgeDocument document, Set<String> queryTerms) {
        Set<String> documentTerms = tokens(document.getTitle() + " " + document.getType() + " " + document.getContent());
        int overlap = 0;
        for (String term : queryTerms) if (documentTerms.contains(term)) overlap++;
        return overlap;
    }

    private Set<String> tokens(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() > 2)
                .collect(Collectors.toSet());
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }

    public record KnowledgeHit(KnowledgeDocument document, int score) {}
}
