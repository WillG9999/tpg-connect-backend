package com.tpg.connect.unit.service;

import com.tpg.connect.email_verification.components.EmailVerificationCodeGenerator;
import com.tpg.connect.email_verification.exceptions.EmailVerificationException;
import com.tpg.connect.email_verification.repository.VerificationCodeRepository;
import com.tpg.connect.email_verification.service.SendVerificationService;
import com.tpg.connect.external.email.client.EmailClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class SendVerificationServiceTest {

    @Test
    void sendVerificationCode_returnsTrue_whenEmailSentAndCodeSaved() {
        EmailClient emailClient = Mockito.mock(EmailClient.class);
        EmailVerificationCodeGenerator generator = Mockito.mock(EmailVerificationCodeGenerator.class);
        VerificationCodeRepository repo = Mockito.mock(VerificationCodeRepository.class);

        Mockito.when(generator.generateCode()).thenReturn("ABC123");
        Mockito.when(emailClient.sendVerificationEmail("test@example.com", "TestUser", "ABC123")).thenReturn(true);
        Mockito.when(repo.saveCode("test@example.com", "ABC123", 1800L)).thenReturn(true);

        SendVerificationService service = new SendVerificationService(emailClient, generator, repo);
        assertTrue(service.sendVerificationCode("test@example.com", "TestUser"));
    }

    @Test
    void sendVerificationCode_throwsException_whenEmailNotSent() {
        EmailClient emailClient = Mockito.mock(EmailClient.class);
        EmailVerificationCodeGenerator generator = Mockito.mock(EmailVerificationCodeGenerator.class);
        VerificationCodeRepository repo = Mockito.mock(VerificationCodeRepository.class);

        Mockito.when(generator.generateCode()).thenReturn("XYZ789");
        Mockito.when(emailClient.sendVerificationEmail("fail@example.com", "FailUser", "XYZ789")).thenReturn(false);

        SendVerificationService service = new SendVerificationService(emailClient, generator, repo);
        assertThrows(EmailVerificationException.class, () ->
                service.sendVerificationCode("fail@example.com", "FailUser"));
    }

    @Test
    void verifyCodeIsCorrect_returnsTrue_whenCodeMatchesAndDeleted() {
        EmailClient emailClient = Mockito.mock(EmailClient.class);
        EmailVerificationCodeGenerator generator = Mockito.mock(EmailVerificationCodeGenerator.class);
        VerificationCodeRepository repo = Mockito.mock(VerificationCodeRepository.class);

        Mockito.when(repo.retrieveCode("test@example.com")).thenReturn("CODE123");
        Mockito.when(repo.deleteCode("test@example.com")).thenReturn(true);

        SendVerificationService service = new SendVerificationService(emailClient, generator, repo);
        assertTrue(service.verifyCodeIsCorrect("test@example.com", "CODE123"));
    }

    @Test
    void verifyCodeIsCorrect_throwsException_whenCodeDoesNotMatch() {
        EmailClient emailClient = Mockito.mock(EmailClient.class);
        EmailVerificationCodeGenerator generator = Mockito.mock(EmailVerificationCodeGenerator.class);
        VerificationCodeRepository repo = Mockito.mock(VerificationCodeRepository.class);

        Mockito.when(repo.retrieveCode("test@example.com")).thenReturn("CODE123");

        SendVerificationService service = new SendVerificationService(emailClient, generator, repo);
        assertThrows(EmailVerificationException.class, () ->
                service.verifyCodeIsCorrect("test@example.com", "WRONGCODE"));
    }
}
