package com.aiqa.platform;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.company.Company;
import com.aiqa.company.CompanyRepository;
import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** M20.1-M20.3 read-only platform-owner oversight. Never exposes password hashes or credential secrets. */
@RestController
@RequestMapping("/api/platform")
public class PlatformOversightController {
    private final CompanyRepository companies;
    private final ApplicationTargetRepository products;
    private final AppUserRepository users;

    public PlatformOversightController(CompanyRepository companies, ApplicationTargetRepository products, AppUserRepository users) {
        this.companies = companies;
        this.products = products;
        this.users = users;
    }

    @GetMapping("/companies")
    public List<CompanyView> companies() {
        return companies.findAll().stream()
                .sorted(Comparator.comparing(Company::getCreatedAt).reversed())
                .map(c -> new CompanyView(c.getId(), c.getName(), c.getSlug(), c.isActive(),
                        products.findByCompanyIdOrderByCreatedAtDesc(c.getId()).size(),
                        users.findByCompanyIdOrderByCreatedAtAsc(c.getId()).size()))
                .toList();
    }

    @GetMapping("/products")
    public List<ProductView> products() {
        return products.findAllByOrderByCreatedAtDesc().stream()
                .map(p -> new ProductView(p.getId(), p.getCompanyId(), p.getName(), p.getEnvironment(), p.getAuthType(), p.isActive()))
                .toList();
    }

    @GetMapping("/users")
    public List<UserView> users() {
        return users.findAll().stream()
                .sorted(Comparator.comparing(AppUser::getCreatedAt).reversed())
                .map(u -> new UserView(u.getId(), u.getCompanyId(), u.getEmail(), u.getRole().name(), u.isActive()))
                .toList();
    }

    public record CompanyView(UUID id, String name, String slug, boolean active, int products, int users) {}
    public record ProductView(UUID id, UUID companyId, String name, String environment, String authType, boolean active) {}
    public record UserView(UUID id, UUID companyId, String email, String role, boolean active) {}
}
