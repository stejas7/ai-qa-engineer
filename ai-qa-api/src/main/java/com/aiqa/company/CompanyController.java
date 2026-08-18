package com.aiqa.company;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/** REST API for company workspace registration and activation. */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyRepository repository;

    public CompanyController(CompanyRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Company> all(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return activeOnly ? repository.findByActiveTrueOrderByCreatedAtDesc()
                : repository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public ResponseEntity<Company> create(@Valid @RequestBody CreateCompanyRequest request) {
        Company company = new Company(request.name(), request.slug());
        if (repository.existsBySlugIgnoreCase(company.getSlug())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Company slug already exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(company));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Company> setActive(@PathVariable UUID id, @RequestParam boolean value) {
        return repository.findById(id).map(company -> {
            company.setActive(value);
            return ResponseEntity.ok(repository.save(company));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record CreateCompanyRequest(@NotBlank String name, String slug) {
    }
}
