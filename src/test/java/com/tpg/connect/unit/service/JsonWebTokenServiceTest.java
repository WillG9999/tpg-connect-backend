package com.tpg.connect.unit.service;

import com.tpg.connect.session_authentication.common.services.JsonWebTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class JsonWebTokenServiceTest {

    private static final String TEST_SECRET = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydGVzdGluZ3B1cnBvc2VzMTIzNDU2";
    private static final long TEST_EXPIRATION = 3600000L;

    private JsonWebTokenService underTest;

    @BeforeEach
    void setUp() {
        underTest = new JsonWebTokenService();
        ReflectionTestUtils.setField(underTest, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(underTest, "accessTokenExpiration", TEST_EXPIRATION);
    }

    @Test
    void generateToken_returnsNonEmptyToken() {
        String token = underTest.generateToken(12345L, "test@example.com");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_tokenContainsCorrectSubject() {
        String token = underTest.generateToken(99999L, "user@example.com");

        Claims claims = parseToken(token);
        assertEquals("99999", claims.getSubject());
    }

    @Test
    void generateToken_tokenContainsEmailClaim() {
        String token = underTest.generateToken(12345L, "john@example.com");

        Claims claims = parseToken(token);
        assertEquals("john@example.com", claims.get("email", String.class));
    }

    @Test
    void generateToken_tokenHasIssuedAt() {
        String token = underTest.generateToken(12345L, "test@example.com");

        Claims claims = parseToken(token);

        assertNotNull(claims.getIssuedAt());
    }

    @Test
    void generateToken_tokenHasExpiration() {
        String token = underTest.generateToken(12345L, "test@example.com");

        Claims claims = parseToken(token);

        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}