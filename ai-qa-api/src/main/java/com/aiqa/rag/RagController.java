package com.aiqa.rag;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/rag")
public class RagController {
    private final RagService service;
    public RagController(RagService service){this.service=service;}

    public record IngestRequest(@NotBlank String content, @NotBlank String source) {}

    @PostMapping("/documents")
    public ResponseEntity<?> ingest(@RequestBody IngestRequest request){
        try { return ResponseEntity.ok(service.ingest(request.content(), request.source())); }
        catch (IllegalStateException e){ return ResponseEntity.status(503).body(Map.of("error",e.getMessage())); }
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q, @RequestParam(defaultValue="5") int limit){
        try { return ResponseEntity.ok(Map.of("query",q,"results",service.search(q,limit))); }
        catch (IllegalStateException e){ return ResponseEntity.status(503).body(Map.of("error",e.getMessage())); }
    }
}
