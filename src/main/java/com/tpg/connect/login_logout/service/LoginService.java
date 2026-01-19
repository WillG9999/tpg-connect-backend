package com.tpg.connect.login_logout.service;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenProvider;
import com.tpg.connect.common.security.RefreshTokenService;
import com.tpg.connect.login_logout.model.response.LoginResponse;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.api.RegisterUserRepositoryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements LoginServiceApi {

    private final RegisterUserRepositoryApi userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JsonWebTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public LoginResponse login(String email, String base64EncodedPassword) {
        log.info("Login attempt for email: {}", email);

        RegisteredUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", email);
                    return new BadCredentialsException("Invalid email or password");
                });

        String decodedPassword = decodeBase64Password(base64EncodedPassword);

        if (!passwordEncoder.matches(decodedPassword, user.password())) {
            log.warn("Login failed - invalid password for user: {}", email);
            throw new BadCredentialsException("Invalid email or password");
        }

        String role = user.role() != null ? user.role() : JsonWebTokenProvider.ROLE_USER;
        String accessToken = tokenProvider.generateToken(user.connectId(), user.email(), role);
        String refreshToken = tokenProvider.generateRefreshToken(user.connectId());

        refreshTokenService.storeRefreshToken(user.connectId(), refreshToken);

        log.info("Login successful for user: {}", email);
        return new LoginResponse(accessToken, refreshToken, tokenProvider.getAccessTokenExpiration(), role);
    }

    private String decodeBase64Password(String base64EncodedPassword) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedPassword);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode base64 password", e);
            throw new BadCredentialsException("Invalid password format");
        }
    }
}
