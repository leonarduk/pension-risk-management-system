package com.leonarduk.finance.springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Simple email sending service leveraging Spring's JavaMail support.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String recipient;

    public EmailService(JavaMailSender mailSender,
            @Value("${portfolio.summary.recipient:test@example.com}") String recipient) {
        this.mailSender = mailSender;
        this.recipient = recipient;
    }

    /**
     * Send an email using the configured {@link JavaMailSender}.
     *
     * @param subject email subject
     * @param body email body
     */
    public void send(String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}

