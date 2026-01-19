package com.tpg.connect.common.services.authentication;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonWebTokenProviderService {

    public static final String ROLE_USER = JsonWebTokenProvider.ROLE_USER;
    public static final String ROLE_ADMIN = JsonWebTokenProvider.ROLE_ADMIN;

    private final JsonWebTokenProvider tokenProvider;

    public String generateToken(long connectId, String email) {
        return tokenProvider.generateToken(connectId, email);
    }

    public String generateToken(long connectId, String email, String role) {
        return tokenProvider.generateToken(connectId, email, role);
    }

    public String generateRefreshToken(long connectId) {
        return tokenProvider.generateRefreshToken(connectId);
    }

    public String generateAdminToken(long connectId, String email) {
        return tokenProvider.generateAdminToken(connectId, email);
    }

    public long getAccessTokenExpiration() {
        return tokenProvider.getAccessTokenExpiration();
    }
}
