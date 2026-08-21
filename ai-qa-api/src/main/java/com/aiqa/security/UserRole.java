package com.aiqa.security;

/**
 * Application roles used by the tenant and platform authorization model.
 *
 * <p>{@code PLATFORM_ADMIN} is retained as a backward-compatible alias for the original M20
 * platform-owner role. New platform-owner accounts use {@code SUPER_ADMIN} from M21 onward.
 */
public enum UserRole {
    SUPER_ADMIN,
    PLATFORM_ADMIN,
    COMPANY_ADMIN,
    QA_MANAGER,
    TESTER,
    VIEWER;

    public boolean isPlatformAdmin() {
        return this == SUPER_ADMIN || this == PLATFORM_ADMIN;
    }

    public boolean isCompanyAdmin() {
        return this == COMPANY_ADMIN;
    }
}
