package com.aiqa.credential;

import com.aiqa.execution.ExecutionRecordRepository;
import com.aiqa.execution.ExecutionRequest;
import com.aiqa.execution.ExecutionResponse;
import com.aiqa.execution.ExecutionService;
import com.aiqa.healing.FailureClassifier;
import com.aiqa.healing.HealingDecisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Browser-level M19 proof: runtime credential -> login -> protected page -> evidence -> PASS. */
class AuthenticatedPlaywrightE2ETest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void usernamePasswordCredentialAuthenticatesAndProducesPassingEvidence() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/login", this::handleLogin);
        server.start();

        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        CredentialProfile profile = new CredentialProfile(
                companyId,
                targetId,
                CredentialProfile.CredentialType.USERNAME_PASSWORD,
                "env:M19_E2E_LOGIN");

        CredentialProfileRepository profiles = mock(CredentialProfileRepository.class);
        when(profiles.findByApplicationTargetId(targetId)).thenReturn(Optional.of(profile));
        RuntimeCredentialResolver resolver = new RuntimeCredentialResolver(
                profiles,
                new ObjectMapper(),
                name -> "M19_E2E_LOGIN".equals(name)
                        ? "{\"username\":\"qa@example.test\",\"password\":\"Secret123456!\"}"
                        : null);

        RuntimeCredentialResolver.ResolvedCredential credential = resolver.resolve(companyId, targetId);
        ExecutionRecordRepository records = mock(ExecutionRecordRepository.class);
        ExecutionService execution = new ExecutionService(
                records,
                mock(FailureClassifier.class),
                mock(HealingDecisionService.class));

        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/login";
        ExecutionResponse result = execution.run(
                new ExecutionRequest("M19-E2E-LOGIN", url, List.of(), "Dashboard ready", true),
                credential);

        assertEquals("PASS", result.status(), result.message());
        assertNotNull(result.screenshot());
        assertTrue(result.screenshot().startsWith("/api/execution/evidence/"));
        verify(records).save(any());
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String html;
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            html = """
                    <!doctype html><html><body>
                    <main><h1>Dashboard ready</h1><p>Authenticated UAT target.</p></main>
                    </body></html>
                    """;
        } else {
            html = """
                    <!doctype html><html><body>
                    <form method="post" action="/login">
                      <label>Email <input type="email" name="username" autocomplete="username"></label>
                      <label>Password <input type="password" name="password"></label>
                      <button type="submit">Sign in</button>
                    </form>
                    </body></html>
                    """;
        }
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
