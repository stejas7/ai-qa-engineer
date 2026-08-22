package com.aiqa.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetDelivery {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetDelivery.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String publicBaseUrl;
    private final String from;

    public PasswordResetDelivery(ObjectProvider<JavaMailSender> mailSenderProvider,
                                 @Value("${AI_UAT_PUBLIC_BASE_URL:https://ai-uat.duckdns.org}") String publicBaseUrl,
                                 @Value("${AI_UAT_MAIL_FROM:no-reply@ai-uat.local}") String from) {
        this.mailSenderProvider = mailSenderProvider;
        this.publicBaseUrl = publicBaseUrl;
        this.from = from;
    }

    public void send(String email, PasswordResetService.ResetTicket ticket) {
        if (ticket == null || ticket.token() == null) return;

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Password reset email not sent because SMTP/JavaMailSender is not configured. Platform startup remains available.");
            return;
        }

        String link = publicBaseUrl + "/reset-password?token=" + ticket.token();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Reset your AI UAT Engineer password");
        message.setText("Use this one-time link within 30 minutes:\n\n" + link + "\n\nIf you did not request this, ignore this email.");
        mailSender.send(message);
    }
}
