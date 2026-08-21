package com.aiqa.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Returns the effective coarse-grained capabilities for the signed-in user.
 * The response is advisory for the UI; server-side authorization remains authoritative.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthorizationCapabilitiesController {

    @GetMapping("/capabilities")
    public Map<String, Object> capabilities(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("VIEWER");

        boolean platform = "SUPER_ADMIN".equals(role) || "PLATFORM_ADMIN".equals(role);
        boolean companyAdmin = "COMPANY_ADMIN".equals(role);
        boolean manager = "QA_MANAGER".equals(role);
        boolean tester = "TESTER".equals(role);

        Map<String, Boolean> permissions = new LinkedHashMap<>();
        permissions.put("platform.read", platform);
        permissions.put("company.users.manage", companyAdmin);
        permissions.put("company.products.read", !platform);
        permissions.put("company.products.manage", companyAdmin || manager);
        permissions.put("company.credentials.manage", companyAdmin || manager);
        permissions.put("uat.run", companyAdmin || manager || tester);
        permissions.put("uat.results.read", !platform);
        permissions.put("automation.manage", companyAdmin || manager);
        permissions.put("automation.execute", companyAdmin || manager || tester);
        permissions.put("testManagement.manage", companyAdmin || manager || tester);
        permissions.put("performance.execute", companyAdmin || manager || tester);
        permissions.put("knowledge.read", !platform);

        return Map.of(
                "role", role,
                "platformOwner", platform,
                "permissions", permissions
        );
    }
}
