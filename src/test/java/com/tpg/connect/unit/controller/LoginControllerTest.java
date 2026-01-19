package com.tpg.connect.unit.controller;

import com.tpg.connect.login_logout.controller.api.LoginController;
import com.tpg.connect.login_logout.model.request.LoginRequest;
import com.tpg.connect.login_logout.model.response.LoginResponse;
import com.tpg.connect.login_logout.service.LoginServiceApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    @Mock
    private LoginServiceApi loginService;

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginController = new LoginController(loginService);
    }

    @Test
    void login_returnsOk_withToken_whenCredentialsValid() {
        LoginRequest request = new LoginRequest("user@example.com", "base64Password");
        LoginResponse loginResponse = new LoginResponse("jwt_token_here", "refresh_token", 900000, "USER");

        when(loginService.login("user@example.com", "base64Password")).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = loginController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt_token_here", response.getBody().accessToken());
        assertEquals("USER", response.getBody().role());
    }

    @Test
    void login_returnsAdminFlag_whenUserIsAdmin() {
        LoginRequest request = new LoginRequest("admin@example.com", "base64Password");
        LoginResponse loginResponse = new LoginResponse("admin_token", "refresh_token", 900000, "ADMIN");

        when(loginService.login("admin@example.com", "base64Password")).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = loginController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ADMIN", response.getBody().role());
    }

    @Test
    void login_throwsBadCredentials_whenInvalidCredentials() {
        LoginRequest request = new LoginRequest("user@example.com", "wrongPassword");

        when(loginService.login("user@example.com", "wrongPassword"))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> loginController.login(request));
    }
}
