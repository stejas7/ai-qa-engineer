package com.aiqa.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Resolves a verified email for OAuth2 providers that may omit email from their default user-info response. */
@Component
public class VerifiedEmailOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private static final Logger log = LoggerFactory.getLogger(VerifiedEmailOAuth2UserService.class);

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        String provider = request.getClientRegistration().getRegistrationId();
        OAuth2User user = delegate.loadUser(request);
        if (!"github".equals(provider) || present(user.getAttribute("email"))) {
            log.debug("SSO user info resolved: provider={} emailPresent={}", provider, present(user.getAttribute("email")));
            return user;
        }

        log.info("SSO verified-email fallback started: provider=github");
        List<Map<String, Object>> emails = restClient.get()
                .uri("https://api.github.com/user/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + request.getAccessToken().getTokenValue())
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        String verifiedEmail = emails == null ? null : emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                .sorted((a, b) -> Boolean.compare(Boolean.TRUE.equals(b.get("primary")), Boolean.TRUE.equals(a.get("primary"))))
                .map(e -> e.get("email"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(VerifiedEmailOAuth2UserService::present)
                .findFirst().orElse(null);

        if (!present(verifiedEmail)) {
            log.warn("SSO verified-email fallback failed: provider=github reason=no_verified_email");
            return user;
        }

        Map<String, Object> attributes = new LinkedHashMap<>(user.getAttributes());
        attributes.put("email", verifiedEmail);
        log.info("SSO verified-email fallback succeeded: provider=github");
        return new DefaultOAuth2User(user.getAuthorities(), attributes, request.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }
}
