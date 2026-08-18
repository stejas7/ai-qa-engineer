package com.aiqa.knowledge;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST API for Scorpion project knowledge ingestion and retrieval. */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    public record AddDocumentRequest(String title, String type, String content, String sourceReference) {}
    public record SearchRequest(String query, Integer limit) {}

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeDocument add(@RequestBody AddDocumentRequest request) {
        if (request == null) throw new IllegalArgumentException("request is required");
        return knowledgeService.add(request.title(), request.type(), request.content(), request.sourceReference());
    }

    @GetMapping("/documents")
    public List<KnowledgeDocument> list() {
        return knowledgeService.list();
    }

    @PostMapping("/search")
    public List<KnowledgeService.KnowledgeHit> search(@RequestBody SearchRequest request) {
        if (request == null) throw new IllegalArgumentException("request is required");
        return knowledgeService.search(request.query(), request.limit() == null ? 5 : request.limit());
    }
}
