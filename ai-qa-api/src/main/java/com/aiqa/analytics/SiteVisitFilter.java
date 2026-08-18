package com.aiqa.analytics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/** Tracks page views using an anonymous browser cookie. Raw IP addresses are not stored. */
@Component
public class SiteVisitFilter extends OncePerRequestFilter {
    private static final String COOKIE_NAME = "auravis_visitor";
    private final SiteVisitRepository visits;

    public SiteVisitFilter(SiteVisitRepository visits) {
        this.visits = visits;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = normalize(request.getRequestURI());
        if ("GET".equalsIgnoreCase(request.getMethod()) && isTrackablePage(path)) {
            String visitorId = findVisitorId(request);
            if (visitorId == null) {
                visitorId = UUID.randomUUID().toString();
                Cookie cookie = new Cookie(COOKIE_NAME, visitorId);
                cookie.setHttpOnly(true);
                cookie.setSecure(request.isSecure());
                cookie.setPath("/");
                cookie.setMaxAge(60 * 60 * 24 * 365);
                cookie.setAttribute("SameSite", "Lax");
                response.addCookie(cookie);
            }
            visits.save(new SiteVisit(path, visitorId, Instant.now()));
        }
        filterChain.doFilter(request, response);
    }

    private String findVisitorId(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .orElse(null);
    }

    private boolean isTrackablePage(String path) {
        return path.equals("/") || path.equals("/index.html") || path.equals("/auravis.html")
                || path.equals("/dashboard.html") || path.equals("/execution-center.html");
    }

    private String normalize(String path) {
        return path == null || path.isBlank() ? "/" : path;
    }
}
