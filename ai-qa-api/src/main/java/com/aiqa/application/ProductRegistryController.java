package com.aiqa.application;

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

/** M16 product/environment API scoped to the authenticated company. */
@RestController
@RequestMapping("/api/company/products")
public class ProductRegistryController {
    private final ProductRegistryService service;

    public ProductRegistryController(ProductRegistryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApplicationTarget> list(Authentication authentication,
                                        @RequestParam(defaultValue = "true") boolean activeOnly) {
        return service.list(authentication.getName(), activeOnly);
    }

    @PostMapping
    public ApplicationTarget create(Authentication authentication,
                                    @RequestBody ProductRegistryService.CreateProductEnvironmentRequest request) {
        return service.create(authentication.getName(), request);
    }

    @PatchMapping("/{id}/active")
    public ApplicationTarget setActive(Authentication authentication,
                                       @PathVariable UUID id,
                                       @RequestParam boolean value) {
        return service.setActive(authentication.getName(), id, value);
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
