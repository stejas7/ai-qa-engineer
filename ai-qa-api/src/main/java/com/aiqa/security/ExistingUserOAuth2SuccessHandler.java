package com.aiqa.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/** Maps a verified OAuth2 email to an existing AI UAT Engineer user; SSO never creates a tenant implicitly. */
@Component
public class ExistingUserOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(ExistingUserOAuth2SuccessHandler.class);

    private final AppUserService users;

    public ExistingUserOAuth2SuccessHandler(AppUserService users) {
        this.users = users;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth)) {
            log.warn("SSO login rejected: authentication type was not OAuth2");
            response.sendRedirect("/account?error=sso");
            return;
        }

        String provider = oauth.getAuthorizedClientRegistrationId();
        Object emailValue = oauth.getPrincipal().getAttributes().get("email");
        if (!(emailValue instanceof String email) || email.isBlank()) {
            log.warn("SSO login rejected: provider={} reason=missing_verified_email", provider);
            response.sendRedirect("/account?error=sso-email");
            return;
        }

        try {
            AppUser user = users.loadActiveUser(email.trim().toLowerCase(Locale.ROOT));
            Authentication mapped = UsernamePasswordAuthenticationToken.authenticated(
                    user.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(mapped);
            SecurityContextHolder.setContext(context);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            log.info("SSO login succeeded: provider={} userId={} companyId={} role={}",
                    provider, user.getId(), user.getCompanyId(), user.getRole());
            response.sendRedirect("/account");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn("SSO login rejected: provider={} reason=existing_active_user_not_found", provider);
            response.sendRedirect("/account?error=sso-not-registered");
        }
    }
}
