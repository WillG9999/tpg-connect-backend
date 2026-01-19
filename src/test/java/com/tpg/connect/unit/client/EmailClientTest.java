package com.tpg.connect.unit.client;

import com.tpg.connect.external.email.client.EmailClient;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailClientTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    private EmailClient emailClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailClient = new EmailClient(mailSender, templateEngine);
        ReflectionTestUtils.setField(emailClient, "fromEmail", "test@example.com");
    }

    @Test
    void sendEmail_returnsTrue_whenEmailSentSuccessfully() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        boolean result = emailClient.sendEmail(
                "recipient@example.com",
                "Test Subject",
                "test-template",
                Map.of("key", "value")
        );

        assertTrue(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_returnsFalse_whenExceptionOccurs() {
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        boolean result = emailClient.sendEmail(
                "test@example.com",
                "Test Subject",
                "test-template",
                Map.of("key", "value")
        );

        assertFalse(result);
    }

    @Test
    void sendVerificationEmail_callsSendEmailWithCorrectParameters() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Code: 123456</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        boolean result = emailClient.sendVerificationEmail("test@example.com", "John", "123456");

        assertTrue(result);
        verify(templateEngine).process(eq("verification-email"), any(Context.class));
    }

    @Test
    void sendPasswordResetEmail_callsSendEmailWithCorrectParameters() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Reset link</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        boolean result = emailClient.sendPasswordResetEmail("test@example.com", "reset-token-uuid");

        assertTrue(result);
        verify(templateEngine).process(eq("password-reset-email"), any(Context.class));
    }

    @Test
    void sendPasswordResetEmail_returnsFalse_whenSendFails() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Reset link</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail send failed")).when(mailSender).send(any(MimeMessage.class));

        boolean result = emailClient.sendPasswordResetEmail("test@example.com", "reset-token-uuid");

        assertFalse(result);
    }
}

