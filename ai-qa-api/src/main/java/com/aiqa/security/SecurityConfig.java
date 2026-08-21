package com.aiqa.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

/** Security foundation with tenant, platform-owner and optional OAuth2 SSO boundaries. */
@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(AppUserRepository users) {
        return username -> {
            AppUser appUser = users.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));
            return User.withUsername(appUser.getEmail()).password(appUser.getPasswordHash())
                    .roles(appUser.getRole().name()).disabled(!appUser.isActive()).build();
        };
    }

    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            ObjectProvider<ClientRegistrationRepository> registrations,
                                            ExistingUserOAuth2SuccessHandler ssoSuccessHandler,
                                            VerifiedEmailOAuth2UserService oauth2UserService) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/sso/**", "/api/auth/password/**").permitAll()
                        .requestMatchers("/api/auth/me", "/api/auth/logout", "/api/auth/capabilities").authenticated()
                        .requestMatchers("/api/platform/**").hasAnyRole("SUPER_ADMIN", "PLATFORM_ADMIN")

                        .requestMatchers("/api/company/users/**").hasRole("COMPANY_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/company/products/**", "/api/company/credentials/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/company/products/**", "/api/company/credentials/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/company/products/**", "/api/company/credentials/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/company/products/**", "/api/company/credentials/**")
                            .hasRole("COMPANY_ADMIN")
                        .requestMatchers("/api/company/products/**", "/api/company/credentials/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER", "TESTER", "VIEWER")
                        .requestMatchers("/api/company/uat/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER", "TESTER")

                        .requestMatchers(HttpMethod.POST, "/api/integrations/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/integrations/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER")
                        .requestMatchers("/api/integrations/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER", "TESTER", "VIEWER")

                        .requestMatchers(HttpMethod.POST, "/api/release-approvals/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/release-approvals/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER")
                        .requestMatchers("/api/release-approvals/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER", "TESTER", "VIEWER")

                        .requestMatchers("/api/agent-workforce/**", "/api/intelligence/**")
                            .authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/automation-scripts/**", "/api/test-management/**", "/api/performance/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER", "TESTER")
                        .requestMatchers(HttpMethod.PUT, "/api/automation-scripts/**", "/api/test-management/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/automation-scripts/**", "/api/test-management/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER", "TESTER")
                        .requestMatchers("/api/automation-scripts/**", "/api/test-management/**", "/api/performance/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER", "TESTER", "VIEWER")

                        .requestMatchers("/api/pipeline/**", "/api/execution/**", "/api/healing/**", "/api/agent-activity/**")
                            .authenticated()
                        .requestMatchers("/api/knowledge/**", "/api/rag/**")
                            .hasAnyRole("COMPANY_ADMIN", "QA_MANAGER", "TESTER", "VIEWER")

                        .requestMatchers("/actuator/health", "/api/ai/runtime", "/api/auth/sso/providers", "/api/analytics/visit").permitAll()
                        .requestMatchers("/api/analytics/**").hasAnyRole("SUPER_ADMIN", "PLATFORM_ADMIN")
                        .requestMatchers("/api/ai/**").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        if (registrations.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/auth/sso/authorization"))
                    .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/auth/sso/callback/*"))
                    .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                    .successHandler(ssoSuccessHandler)
                    .failureUrl("/account?error=sso"));
        }
        return http.build();
    }
}
