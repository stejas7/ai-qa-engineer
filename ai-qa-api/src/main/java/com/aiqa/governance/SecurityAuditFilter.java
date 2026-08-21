package com.aiqa.governance;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Captures metadata-only audit records for authenticated API mutations. */
@Component
public class SecurityAuditFilter extends OncePerRequestFilter {
    private static final Set<String> MUTATING = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final SecurityAuditEventRepository events;

    public SecurityAuditFilter(SecurityAuditEventRepository events) {
        this.events = events;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) correlationId = UUID.randomUUID().toString();
        response.setHeader("X-Correlation-Id", correlationId);

        filterChain.doFilter(request, response);

        if (!request.getRequestURI().startsWith("/api/") || !MUTATING.contains(request.getMethod())) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return;

        String safePath = request.getRequestURI();
        if (safePath.length() > 500) safePath = safePath.substring(0, 500);
        try {
            events.save(new SecurityAuditEvent(auth.getName(), request.getMethod(), safePath,
                    response.getStatus(), correlationId, Instant.now()));
        } catch (RuntimeException ignored) {
            // Auditing must not make a successful product request fail.
        }
    }
}
