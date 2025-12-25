package com.tpg.connect.external.email.client;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailClient {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public boolean sendEmail(String toEmail, String subject, String templateName, Map<String, Object> model) {
        try {
            Context context = new Context();
            context.setVariables(model);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
            return false; //todo: thow an expcetion here
        }
    }

    public boolean sendVerificationEmail(String toEmail, String userName, String verificationCode) {
        Map<String, Object> model = Map.of(
                "userName", userName,
                "verificationCode", verificationCode
        );
        return sendEmail(toEmail, "Email Verification Code", "verification-email", model);
    }
}
