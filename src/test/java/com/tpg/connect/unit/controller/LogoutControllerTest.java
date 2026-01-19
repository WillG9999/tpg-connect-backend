package com.tpg.connect.unit.controller;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenValidator;
import com.tpg.connect.common.security.RefreshTokenService;
import com.tpg.connect.common.security.TokenBlacklistService;
import com.tpg.connect.login_logout.controller.api.LogoutController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LogoutControllerTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JsonWebTokenValidator tokenValidator;

    private LogoutController logoutController;

    @BeforeEach
    void setUp() {
        logoutController = new LogoutController(tokenBlacklistService, refreshTokenService, tokenValidator);
    }

    @Test
    void logout_returnsOk() {
        ResponseEntity<Void> response = logoutController.logout();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void logout_returnsEmptyBody() {
        ResponseEntity<Void> response = logoutController.logout();

        assertNull(response.getBody());
    }
}
