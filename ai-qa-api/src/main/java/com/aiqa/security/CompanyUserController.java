package com.aiqa.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Company-admin API for M15 tenant-scoped user administration. */
@RestController
@RequestMapping("/api/company/users")
public class CompanyUserController {
    private final CompanyUserService service;

    public CompanyUserController(CompanyUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<CompanyUserService.UserSummary> list(Authentication authentication) {
        return service.listUsers(authentication.getName());
    }

    @PostMapping
    public CompanyUserService.UserSummary create(Authentication authentication,
                                                 @RequestBody CompanyUserService.CreateUserRequest request) {
        return service.createUser(authentication.getName(), request);
    }

    @PatchMapping("/{id}/deactivate")
    public CompanyUserService.UserSummary deactivate(Authentication authentication, @PathVariable UUID id) {
        return service.deactivateUser(authentication.getName(), id);
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
