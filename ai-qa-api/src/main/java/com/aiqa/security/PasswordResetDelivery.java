package com.aiqa.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetDelivery {
    private final JavaMailSender mailSender;
    private final String publicBaseUrl;
    private final String from;

    public PasswordResetDelivery(JavaMailSender mailSender,
                                 @Value("${AI_UAT_PUBLIC_BASE_URL:https://ai-uat.duckdns.org}") String publicBaseUrl,
                                 @Value("${AI_UAT_MAIL_FROM:no-reply@ai-uat.local}") String from) {
        this.mailSender = mailSender;
        this.publicBaseUrl = publicBaseUrl;
        this.from = from;
    }

    public void send(String email, PasswordResetService.ResetTicket ticket) {
        if (ticket == null || ticket.token() == null) return;
        String link = publicBaseUrl + "/reset-password?token=" + ticket.token();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Reset your AI UAT Engineer password");
        message.setText("Use this one-time link within 30 minutes:\n\n" + link + "\n\nIf you did not request this, ignore this email.");
        mailSender.send(message);
    }
}
