package com.tpg.connect.login_logout.controller.api;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenValidator;
import com.tpg.connect.common.security.RefreshTokenService;
import com.tpg.connect.common.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LogoutController implements LogoutApi {

    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final JsonWebTokenValidator tokenValidator;

    @Override
    public ResponseEntity<Void> logout() {
        log.info("Logout request received");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() != null) {
            String token = auth.getCredentials().toString();

            try {
                long connectId = tokenValidator.extractConnectId(token);
                refreshTokenService.revokeRefreshToken(connectId);
                tokenBlacklistService.blacklistToken(token, 900);
                log.info("User {} logged out, tokens invalidated", connectId);
            } catch (Exception e) {
                log.warn("Could not invalidate tokens: {}", e.getMessage());
            }
        }

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}
