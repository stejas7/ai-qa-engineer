package com.aiqa.security;

import com.aiqa.company.Company;
import com.aiqa.company.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Creates a company workspace and its first COMPANY_ADMIN atomically. */
@Service
public class CompanyRegistrationService {
    private final CompanyRepository companies;
    private final AppUserService users;

    public CompanyRegistrationService(CompanyRepository companies, AppUserService users) {
        this.companies = companies;
        this.users = users;
    }

    @Transactional
    public RegistrationResult register(RegisterCompanyRequest request) {
        if (request == null) throw new IllegalArgumentException("registration request is required");
        String name = required(request.companyName(), "companyName");
        String slug = normalizeSlug(request.slug() == null || request.slug().isBlank() ? name : request.slug());
        if (companies.existsBySlugIgnoreCase(slug)) throw new IllegalStateException("Company is already registered");

        Company company = companies.save(new Company(name, slug));
        AppUser admin = users.createCompanyAdmin(company.getId(), request.adminEmail(), request.password());
        return new RegistrationResult(company.getId(), company.getName(), company.getSlug(), admin.getEmail(), admin.getRole().name());
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private String normalizeSlug(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (normalized.isBlank()) throw new IllegalArgumentException("company slug must contain letters or numbers");
        return normalized;
    }

    public record RegisterCompanyRequest(String companyName, String slug, String adminEmail, String password) {}
    public record RegistrationResult(java.util.UUID companyId, String companyName, String slug, String adminEmail, String role) {}
}
