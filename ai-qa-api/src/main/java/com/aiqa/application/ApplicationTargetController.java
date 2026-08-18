package com.aiqa.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationTargetController {
    private final ApplicationTargetRepository repository;

    public ApplicationTargetController(ApplicationTargetRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ApplicationTarget> all(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return activeOnly ? repository.findByActiveTrueOrderByCreatedAtDesc()
                : repository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public ApplicationTarget create(@Valid @RequestBody CreateApplicationRequest request) {
        return repository.save(new ApplicationTarget(
                request.name(), request.baseUrl(), request.environment(), request.authType()));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApplicationTarget> setActive(@PathVariable UUID id, @RequestParam boolean value) {
        return repository.findById(id).map(target -> {
            target.setActive(value);
            return ResponseEntity.ok(repository.save(target));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record CreateApplicationRequest(@NotBlank String name,
                                           @NotBlank String baseUrl,
                                           String environment,
                                           String authType) {}
}
