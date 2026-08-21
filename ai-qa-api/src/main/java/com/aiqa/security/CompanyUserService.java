package com.aiqa.security;

import com.aiqa.governance.TenantGovernanceService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Tenant-safe company user administration with multi-admin and governance safeguards. */
@Service
public class CompanyUserService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TenantGovernanceService governance;

    public CompanyUserService(AppUserRepository users,
                              PasswordEncoder passwordEncoder,
                              TenantGovernanceService governance) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.governance = governance;
    }

    public List<UserSummary> listUsers(String actorEmail) {
        AppUser actor = requireCompanyAdmin(actorEmail);
        return users.findByCompanyIdOrderByCreatedAtAsc(actor.getCompanyId()).stream()
                .map(UserSummary::from)
                .toList();
    }

    public UserSummary createUser(String actorEmail, CreateUserRequest request) {
        AppUser actor = requireCompanyAdmin(actorEmail);
        if (request == null) throw new IllegalArgumentException("user request is required");
        String email = normalizeEmail(request.email());
        validatePassword(request.password());
        UserRole role = validateManagedRole(request.role());
        if (users.existsByEmailIgnoreCase(email)) throw new IllegalStateException("User email is already registered");
        governance.assertCanAddUser(actor.getCompanyId());
        AppUser created = users.save(new AppUser(actor.getCompanyId(), email, passwordEncoder.encode(request.password()), role));
        return UserSummary.from(created);
    }

    public UserSummary changeRole(String actorEmail, UUID userId, String rawRole) {
        AppUser actor = requireCompanyAdmin(actorEmail);
        AppUser target = requireSameTenant(actor, userId);
        UserRole nextRole = validateManagedRole(rawRole);
        if (target.getRole() == UserRole.COMPANY_ADMIN && nextRole != UserRole.COMPANY_ADMIN) {
            ensureAnotherActiveCompanyAdmin(actor.getCompanyId(), target.getId());
        }
        target.changeRole(nextRole);
        return UserSummary.from(users.save(target));
    }

    public UserSummary deactivateUser(String actorEmail, UUID userId) {
        AppUser actor = requireCompanyAdmin(actorEmail);
        AppUser target = requireSameTenant(actor, userId);
        if (actor.getId() != null && actor.getId().equals(target.getId())) {
            throw new IllegalStateException("Company admin cannot deactivate their own account");
        }
        if (target.getRole() == UserRole.COMPANY_ADMIN) {
            ensureAnotherActiveCompanyAdmin(actor.getCompanyId(), target.getId());
        }
        target.deactivate();
        return UserSummary.from(users.save(target));
    }

    public UserSummary activateUser(String actorEmail, UUID userId) {
        AppUser actor = requireCompanyAdmin(actorEmail);
        AppUser target = requireSameTenant(actor, userId);
        target.activate();
        return UserSummary.from(users.save(target));
    }

    private AppUser requireSameTenant(AppUser actor, UUID userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        AppUser target = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!actor.getCompanyId().equals(target.getCompanyId())) throw new SecurityException("Cross-tenant user access denied");
        if (target.getRole().isPlatformAdmin()) throw new SecurityException("Platform owner accounts cannot be managed by company admins");
        return target;
    }

    private void ensureAnotherActiveCompanyAdmin(UUID companyId, UUID excludedId) {
        long activeAdmins = users.findByCompanyIdOrderByCreatedAtAsc(companyId).stream()
                .filter(AppUser::isActive)
                .filter(user -> user.getRole() == UserRole.COMPANY_ADMIN)
                .filter(user -> excludedId == null || !excludedId.equals(user.getId()))
                .count();
        if (activeAdmins == 0) throw new IllegalStateException("At least one active Company Admin must remain");
    }

    private AppUser requireCompanyAdmin(String email) {
        AppUser actor = users.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        if (!actor.isActive()) throw new IllegalStateException("User is inactive");
        if (actor.getRole() != UserRole.COMPANY_ADMIN) throw new SecurityException("Company admin role required");
        return actor;
    }

    private UserRole validateManagedRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) throw new IllegalArgumentException("role is required");
        final UserRole role;
        try { role = UserRole.valueOf(rawRole.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Unsupported role"); }
        if (role.isPlatformAdmin()) {
            throw new IllegalArgumentException("Platform owner roles cannot be assigned from a company tenant");
        }
        return role;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!normalized.contains("@") || normalized.startsWith("@") || normalized.endsWith("@")) {
            throw new IllegalArgumentException("email is invalid");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12) throw new IllegalArgumentException("password must contain at least 12 characters");
    }

    public record CreateUserRequest(String email, String password, String role) {}
    public record ChangeRoleRequest(String role) {}
    public record UserSummary(UUID id, UUID companyId, String email, String role, boolean active) {
        static UserSummary from(AppUser user) {
            return new UserSummary(user.getId(), user.getCompanyId(), user.getEmail(), user.getRole().name(), user.isActive());
        }
    }
}
