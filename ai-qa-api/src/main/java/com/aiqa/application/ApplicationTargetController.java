package com.aiqa.application;

import com.aiqa.company.Company;
import com.aiqa.company.CompanyRepository;
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

@RestController
@RequestMapping("/api/applications")
public class ApplicationTargetController {
    private final ApplicationTargetRepository repository;
    private final CompanyRepository companyRepository;

    public ApplicationTargetController(ApplicationTargetRepository repository, CompanyRepository companyRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
    }

    @GetMapping
    public List<ApplicationTarget> all(@RequestParam(defaultValue = "false") boolean activeOnly,
                                       @RequestParam(required = false) UUID companyId) {
        if (companyId != null) {
            return activeOnly ? repository.findByCompanyIdAndActiveTrueOrderByCreatedAtDesc(companyId)
                    : repository.findByCompanyIdOrderByCreatedAtDesc(companyId);
        }
        return activeOnly ? repository.findByActiveTrueOrderByCreatedAtDesc()
                : repository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public ApplicationTarget create(@Valid @RequestBody CreateApplicationRequest request) {
        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown companyId"));
            if (!company.isActive()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Cannot register a product under an inactive company");
            }
            if (repository.existsByCompanyIdAndNameIgnoreCase(request.companyId(), request.name().trim())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A product with this name already exists for the company");
            }
        }
        return repository.save(new ApplicationTarget(
                request.name().trim(), request.baseUrl(), request.environment(), request.authType(), request.companyId()));
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
                                           String authType,
                                           UUID companyId) {}
}
