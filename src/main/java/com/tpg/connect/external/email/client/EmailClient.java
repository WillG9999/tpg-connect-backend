package com.tpg.connect.external.email.client;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailClient {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@tpg.com}")
    private String fromEmail;

    public boolean sendEmail(String toEmail, String subject, String template, Map<String, String> variables) {
        try {
            Context context = new Context();
            if (variables != null) variables.forEach(context::setVariable);
            String html = templateEngine.process(template, context);
            send(toEmail, subject, html);
            log.info("Email sent - template:: {} to:: {}", template, toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email - template:: {} to:: {}", template, toEmail, e);
            return false;
        }
    }

    public boolean sendVerificationEmail(String toEmail, String userName, String verificationCode) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("verificationCode", verificationCode);
            String html = templateEngine.process("verification-email", context);
            send(toEmail, "Verify your TPG Connect email", html);
            log.info("Verification email sent to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            return false;
        }
    }

    public boolean sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("resetLink", resetToken);
            context.setVariable("expiryHours", 1);
            String html = templateEngine.process("password-reset-email", context);
            send(toEmail, "Reset your TPG Connect password", html);
            log.info("Password reset email sent to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            return false;
        }
    }

    public boolean sendApplicationApprovalEmail(String toEmail, String firstName) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("loginLink", "https://tpgconnect.app/login");
            String html = templateEngine.process("application-approved-email", context);
            send(toEmail, "Welcome to TPG Connect!", html);
            log.info("Approval email sent to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send approval email to: {}", toEmail, e);
            return false;
        }
    }

    private void send(String toEmail, String subject, String htmlBody) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}
