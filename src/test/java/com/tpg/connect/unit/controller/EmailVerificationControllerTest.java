package com.tpg.connect.unit.controller;

import com.tpg.connect.email_verification.components.EmailVerificationCodeGenerator;
import com.tpg.connect.email_verification.exceptions.EmailVerificationException;
import com.tpg.connect.email_verification.repository.VerificationCodeRepository;
import com.tpg.connect.email_verification.service.SendVerificationService;
import com.tpg.connect.external.email.client.EmailClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationControllerTest {

    @Test
    void sendVerificationCode_returnsTrue_whenEmailSentAndCodeSaved() {
        EmailClient emailClient = Mockito.mock(EmailClient.class);
        EmailVerificationCodeGenerator generator = Mockito.mock(EmailVerificationCodeGenerator.class);
        VerificationCodeRepository repo = Mockito.mock(VerificationCodeRepository.class);

        Mockito.when(generator.generateCode()).thenReturn("CODE123");
        Mockito.when(emailClient.sendVerificationEmail("user@example.com", "User", "CODE123")).thenReturn(true);
        Mockito.when(repo.saveCode("user@example.com", "CODE123", 1800L)).thenReturn(true);

        SendVerificationService service = new SendVerificationService(emailClient, generator, repo);

        assertTrue(service.sendVerificationCode("user@example.com", "User"));
    }

    @Test
    void sendVerificationCode_throwsException_whenEmailNotSent() {
        EmailClient emailClient = Mockito.mock(EmailClient.class);
        EmailVerificationCodeGenerator generator = Mockito.mock(EmailVerificationCodeGenerator.class);
        VerificationCodeRepository repo = Mockito.mock(VerificationCodeRepository.class);

        Mockito.when(generator.generateCode()).thenReturn("CODE123");
        Mockito.when(emailClient.sendVerificationEmail("fail@example.com", "FailUser", "CODE123")).thenReturn(false);

        SendVerificationService service = new SendVerificationService(emailClient, generator, repo);

        assertThrows(EmailVerificationException.class, () ->
                service.sendVerificationCode("fail@example.com", "FailUser"));
    }
}
