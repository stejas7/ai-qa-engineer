package com.aiqa.pipeline;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/** M18 authenticated product-scoped UAT launch API. */
@RestController
@RequestMapping("/api/company/uat")
public class TenantUatLaunchController {
    private final TenantUatLaunchService service;

    public TenantUatLaunchController(TenantUatLaunchService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(Authentication authentication,
                                    @RequestParam UUID targetId,
                                    @RequestParam("file") MultipartFile file) {
        PipelineRun run = service.launch(authentication.getName(), targetId, file);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("runId", run.getId(), "status", run.getStatus()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String,String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    ResponseEntity<Map<String,String>> forbidden(SecurityException e) {
        return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
    }
}
