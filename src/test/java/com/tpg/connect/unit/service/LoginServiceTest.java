package com.tpg.connect.unit.service;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenProvider;
import com.tpg.connect.common.security.RefreshTokenService;
import com.tpg.connect.login_logout.model.response.LoginResponse;
import com.tpg.connect.login_logout.service.LoginService;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.api.RegisterUserRepositoryApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    @Mock
    private RegisterUserRepositoryApi userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JsonWebTokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginService = new LoginService(userRepository, passwordEncoder, tokenProvider, refreshTokenService);
    }

    @Test
    void login_returnsToken_whenCredentialsValid() {
        String email = "user@example.com";
        String rawPassword = "password123";
        String base64Password = Base64.getEncoder().encodeToString(rawPassword.getBytes());

        RegisteredUser user = RegisteredUser.builder()
                .connectId(123456L)
                .email(email)
                .password("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .role("USER")
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateToken(123456L, email, "USER")).thenReturn("jwt_token_here");
        when(tokenProvider.generateRefreshToken(123456L)).thenReturn("refresh_token");
        when(tokenProvider.getAccessTokenExpiration()).thenReturn(900000L);

        LoginResponse response = loginService.login(email, base64Password);

        assertNotNull(response);
        assertEquals("jwt_token_here", response.accessToken());
        assertEquals("refresh_token", response.refreshToken());
        assertEquals("USER", response.role());
    }

    @Test
    void login_throwsBadCredentials_whenUserNotFound() {
        String email = "notfound@example.com";
        String base64Password = Base64.getEncoder().encodeToString("password".getBytes());

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> loginService.login(email, base64Password));
    }

    @Test
    void login_throwsBadCredentials_whenPasswordIncorrect() {
        String email = "user@example.com";
        String rawPassword = "wrongpassword";
        String base64Password = Base64.getEncoder().encodeToString(rawPassword.getBytes());

        RegisteredUser user = RegisteredUser.builder()
                .connectId(123456L)
                .email(email)
                .password("hashedPassword")
                .role("USER")
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "hashedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> loginService.login(email, base64Password));
    }

    @Test
    void login_throwsBadCredentials_whenBase64Invalid() {
        String email = "user@example.com";
        String invalidBase64 = "not-valid-base64!!!";

        RegisteredUser user = RegisteredUser.builder()
                .connectId(123456L)
                .email(email)
                .password("hashedPassword")
                .role("USER")
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class, () -> loginService.login(email, invalidBase64));
    }

    @Test
    void login_decodesBase64Password_correctly() {
        String email = "user@example.com";
        String rawPassword = "mySecurePass123!";
        String base64Password = Base64.getEncoder().encodeToString(rawPassword.getBytes());

        RegisteredUser user = RegisteredUser.builder()
                .connectId(123456L)
                .email(email)
                .password("hashedPassword")
                .role("USER")
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateToken(anyLong(), anyString(), anyString())).thenReturn("token");
        when(tokenProvider.generateRefreshToken(anyLong())).thenReturn("refresh");
        when(tokenProvider.getAccessTokenExpiration()).thenReturn(900000L);

        loginService.login(email, base64Password);

        verify(passwordEncoder).matches(rawPassword, "hashedPassword");
    }
}
