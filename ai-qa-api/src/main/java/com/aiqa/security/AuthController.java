package com.aiqa.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/** M14 authentication API. Password material is accepted only at register/login and never returned. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppUserService users;
    private final CompanyRegistrationService registrations;
    private final AuthenticationManager authenticationManager;

    public AuthController(AppUserService users,
                          CompanyRegistrationService registrations,
                          AuthenticationManager authenticationManager) {
        this.users = users;
        this.registrations = registrations;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public CompanyRegistrationService.RegistrationResult register(
            @RequestBody CompanyRegistrationService.RegisterCompanyRequest request) {
        return registrations.register(request);
    }

    @PostMapping("/login")
    public CurrentUser login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return current(authentication);
    }

    @GetMapping("/me")
    public CurrentUser me(Authentication authentication) {
        return current(authentication);
    }

    @PostMapping("/logout")
    public Map<String, Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return Map.of("loggedOut", true);
    }

    private CurrentUser current(Authentication authentication) {
        AppUser user = users.loadActiveUser(authentication.getName());
        return new CurrentUser(user.getId(), user.getCompanyId(), user.getEmail(), user.getRole().name());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String,String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }

    public record LoginRequest(String email, String password) {}
    public record CurrentUser(UUID id, UUID companyId, String email, String role) {}
}
