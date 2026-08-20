package com.aiqa.credential;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** M17 credential profile API. Secret values and secret references are never returned. */
@RestController
@RequestMapping("/api/company/credentials")
public class CredentialProfileController {
    private final CredentialProfileService service;

    public CredentialProfileController(CredentialProfileService service) {
        this.service = service;
    }

    @GetMapping
    public List<CredentialProfileService.CredentialProfileSummary> list(Authentication authentication) {
        return service.list(authentication.getName());
    }

    @PostMapping
    public CredentialProfileService.CredentialProfileSummary configure(
            Authentication authentication,
            @RequestBody CredentialProfileService.ConfigureCredentialProfileRequest request) {
        return service.configure(authentication.getName(), request);
    }

    @PatchMapping("/{id}/active")
    public CredentialProfileService.CredentialProfileSummary setActive(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestParam boolean value) {
        return service.setActive(authentication.getName(), id, value);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    ResponseEntity<Map<String, String>> forbidden(SecurityException e) {
        return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
    }
}
