package com.tpg.connect.login_logout.controller;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenProvider;
import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenValidator;
import com.tpg.connect.common.security.RefreshTokenService;
import com.tpg.connect.login_logout.controller.api.RefreshTokenApi;
import com.tpg.connect.login_logout.model.request.RefreshTokenRequest;
import com.tpg.connect.login_logout.model.response.RefreshTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenController implements RefreshTokenApi {

    private final RefreshTokenService refreshTokenService;
    private final JsonWebTokenValidator tokenValidator;
    private final JsonWebTokenProvider tokenProvider;

    @Override
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");

        String refreshToken = request.refreshToken();

        String email = null;
        String role = "USER";

        try {
            email = tokenValidator.extractEmail(refreshToken);
            role = tokenValidator.getRole(refreshToken);
        } catch (Exception e) {
            log.debug("Could not extract email/role from refresh token");
        }

        return refreshTokenService.refreshAccessToken(refreshToken, email, role != null ? role : "USER")
                .map(tokenPair -> {
                    log.info("Token refresh successful");
                    return ResponseEntity.ok(new RefreshTokenResponse(
                            tokenPair.accessToken(),
                            tokenPair.refreshToken(),
                            tokenProvider.getAccessTokenExpiration()
                    ));
                })
                .orElseGet(() -> {
                    log.warn("Token refresh failed - invalid refresh token");
                    return ResponseEntity.status(401).build();
                });
    }
}

