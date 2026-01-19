package com.tpg.connect.unit.service;

import com.tpg.connect.common.services.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordService = new PasswordService(passwordEncoder);
    }

    @Test
    void hashPassword_returnsHashedPassword() {
        String rawPassword = "password123";
        String base64Password = Base64.getEncoder().encodeToString(rawPassword.getBytes());
        when(passwordEncoder.encode(rawPassword)).thenReturn("$2a$10$hashedValue");

        String result = passwordService.hashPassword(base64Password);

        assertEquals("$2a$10$hashedValue", result);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void matches_returnsTrue_whenPasswordMatches() {
        String rawPassword = "password123";
        String hashedPassword = "$2a$10$hashedValue";
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);

        boolean result = passwordService.matches(rawPassword, hashedPassword);

        assertTrue(result);
    }

    @Test
    void matches_returnsFalse_whenPasswordDoesNotMatch() {
        String rawPassword = "wrongPassword";
        String hashedPassword = "$2a$10$hashedValue";
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        boolean result = passwordService.matches(rawPassword, hashedPassword);

        assertFalse(result);
    }

    @Test
    void decodeBase64Password_decodesCorrectly() {
        String rawPassword = "myPassword123";
        String base64Password = Base64.getEncoder().encodeToString(rawPassword.getBytes());

        String result = passwordService.decodeBase64Password(base64Password);

        assertEquals(rawPassword, result);
    }

    @Test
    void decodeBase64Password_handlesSpecialCharacters() {
        String rawPassword = "p@ssw0rd!#$%";
        String base64Password = Base64.getEncoder().encodeToString(rawPassword.getBytes());

        String result = passwordService.decodeBase64Password(base64Password);

        assertEquals(rawPassword, result);
    }

    @Test
    void decodeBase64Password_handlesEmptyString() {
        String rawPassword = "";
        String base64Password = Base64.getEncoder().encodeToString(rawPassword.getBytes());

        String result = passwordService.decodeBase64Password(base64Password);

        assertEquals(rawPassword, result);
    }
}

