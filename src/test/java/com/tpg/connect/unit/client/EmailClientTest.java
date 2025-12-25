package com.tpg.connect.unit.client;

import com.tpg.connect.external.email.client.EmailClient;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmailClientTest {

    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;
    private EmailClient emailClient;

    @BeforeEach
    void setUp() throws Exception {
        mailSender = Mockito.mock(JavaMailSender.class);
        templateEngine = Mockito.mock(TemplateEngine.class);
        emailClient = new EmailClient(mailSender, templateEngine);

        try {
            var fromEmailField = EmailClient.class.getDeclaredField("fromEmail");
            fromEmailField.setAccessible(true);
            fromEmailField.set(emailClient, "noreply@example.com");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendEmail_success_returnsTrue() {
        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        Mockito.when(templateEngine.process(Mockito.anyString(), Mockito.any(Context.class))).thenReturn("<html>Email</html>");

        boolean result = emailClient.sendEmail("to@example.com", "Subject", "template", Map.of("key", "value"));
        assertTrue(result);
    }

    @Test
    void sendEmail_messagingException_returnsFalse() {
        Mockito.when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("fail"));
        Mockito.when(templateEngine.process(Mockito.anyString(), Mockito.any(Context.class))).thenReturn("<html>Email</html>");

        boolean result = emailClient.sendEmail("to@example.com", "Subject", "template", Map.of("key", "value"));
        assertFalse(result);
    }

    @Test
    void sendVerificationEmail_delegatesToSendEmail() {
        EmailClient spyClient = Mockito.spy(emailClient);
        Mockito.doReturn(true).when(spyClient).sendEmail(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyMap()
        );
        boolean result = spyClient.sendVerificationEmail("to@example.com", "User", "CODE123");
        assertTrue(result);
        Mockito.verify(spyClient).sendEmail(
                "to@example.com",
                "Email Verification Code",
                "verification-email",
                Map.of("userName", "User", "verificationCode", "CODE123")
        );
    }
}
