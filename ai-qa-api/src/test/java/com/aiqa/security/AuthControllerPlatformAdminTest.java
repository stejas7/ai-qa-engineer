package com.aiqa.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Regression contract for the platform-owner username/password login path. */
@ExtendWith(MockitoExtension.class)
class AuthControllerPlatformAdminTest {
    @Mock AppUserService users;
    @Mock CompanyRegistrationService registrations;
    @Mock AuthenticationManager authenticationManager;
    @Mock Authentication authentication;

    @Test
    void platformAdminLoginCreatesAuthenticatedSessionAndReturnsPlatformRole() {
        String email = "platform-admin@example.test";
        UUID platformTenant = UUID.fromString("00000000-0000-0000-0000-000000000001");
        AppUser platformAdmin = new AppUser(platformTenant, email, "bcrypt-hash", UserRole.PLATFORM_ADMIN);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(users.loadActiveUser(email)).thenReturn(platformAdmin);

        AuthController controller = new AuthController(users, registrations, authenticationManager);
        MockHttpServletRequest request = new MockHttpServletRequest();

        AuthController.CurrentUser current = controller.login(
                new AuthController.LoginRequest(email, "correct-horse-battery-staple"), request);

        assertEquals(email, current.email());
        assertEquals(platformTenant, current.companyId());
        assertEquals("PLATFORM_ADMIN", current.role());
        assertNotNull(request.getSession(false));
        assertNotNull(request.getSession(false).getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
    }
}
