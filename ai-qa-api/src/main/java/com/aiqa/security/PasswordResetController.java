package com.aiqa.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {
    private final PasswordResetService service;
    private final PasswordResetDelivery delivery;

    public PasswordResetController(PasswordResetService service, PasswordResetDelivery delivery) {
        this.service = service;
        this.delivery = delivery;
    }

    @PostMapping("/forgot")
    public Map<String, String> forgot(@RequestBody ForgotPasswordRequest request) {
        String email = request == null ? null : request.email();
        PasswordResetService.ResetTicket ticket = service.request(email);
        if (email != null && !email.isBlank()) delivery.send(email.trim(), ticket);
        return Map.of("message", "If the account exists, a password reset link has been sent.");
    }

    @PostMapping("/reset")
    public Map<String, String> reset(@RequestBody ResetPasswordRequest request) {
        if (request == null) throw new IllegalArgumentException("reset request is required");
        service.reset(request.token(), request.password());
        return Map.of("message", "Password reset complete. You can sign in now.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    public record ForgotPasswordRequest(String email) {}
    public record ResetPasswordRequest(String token, String password) {}
}
