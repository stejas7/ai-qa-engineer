package com.aiqa.session;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.company.Company;
import com.aiqa.company.CompanyRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** REST API for M10 company/product-scoped UAT sessions. */
@RestController
@RequestMapping("/api/uat-sessions")
public class UatSessionController {
    private final UatSessionRepository repository;
    private final CompanyRepository companyRepository;
    private final ApplicationTargetRepository applicationRepository;

    public UatSessionController(UatSessionRepository repository,
                                CompanyRepository companyRepository,
                                ApplicationTargetRepository applicationRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping
    public List<UatSession> all(@RequestParam(required = false) UUID companyId,
                                @RequestParam(required = false) UUID applicationId) {
        if (companyId != null && applicationId != null) {
            return repository.findByCompanyIdAndApplicationIdOrderByCreatedAtDesc(companyId, applicationId);
        }
        if (companyId != null) {
            return repository.findByCompanyIdOrderByCreatedAtDesc(companyId);
        }
        if (applicationId != null) {
            return repository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
        }
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public UatSession create(@Valid @RequestBody CreateUatSessionRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown companyId"));
        if (!company.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot create a UAT session for an inactive company");
        }

        ApplicationTarget application = applicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown applicationId"));
        if (!application.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot create a UAT session for an inactive product");
        }
        if (!Objects.equals(application.getCompanyId(), request.companyId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Product does not belong to the selected company");
        }

        return repository.save(new UatSession(
                request.companyId(), request.applicationId(), request.buildVersion(), request.objective()));
    }

    public record CreateUatSessionRequest(@NotNull UUID companyId,
                                          @NotNull UUID applicationId,
                                          String buildVersion,
                                          @NotBlank String objective) {
    }
}
