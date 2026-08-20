package com.aiqa.pipeline;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pipeline/runs")
public class PipelineRerunController {
    private final PipelineRerunService service;

    public PipelineRerunController(PipelineRerunService service) {
        this.service = service;
    }

    @PostMapping("/{id}/rerun")
    public ResponseEntity<?> rerun(@PathVariable UUID id) {
        PipelineRun run = service.rerun(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("runId", run.getId(), "status", run.getStatus()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String,String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }
}
