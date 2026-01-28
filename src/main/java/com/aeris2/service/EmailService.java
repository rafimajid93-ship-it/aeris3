// src/main/java/com/aeris2/service/EmailService.java
package com.aeris2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            log.info("✅ Order email sent to {}", to);
        } catch (MailException ex) {
            // ⛔ IMPORTANT: swallow the exception so order API doesn’t fail
            log.error("⚠️ Failed to send email to {}. Continuing without email.", to, ex);
        }
    }
}
