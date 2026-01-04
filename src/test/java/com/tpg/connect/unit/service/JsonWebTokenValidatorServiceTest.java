package com.tpg.connect.unit.service;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JsonWebTokenValidatorServiceTest {

    private static final String TEST_SECRET = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydGVzdGluZ3B1cnBvc2VzMTIzNDU2";
    private static final long EXPIRATION = 3600000L;

    private JsonWebTokenValidator underTest;
    private String validToken;

    @BeforeEach
    void setUp() {
        underTest = new JsonWebTokenValidator();
        ReflectionTestUtils.setField(underTest, "secretKey", TEST_SECRET);

        validToken = Jwts.builder()
                .subject("testuser")
                .claim("email", "test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getTestKey())
                .compact();
    }

    @Test
    void isValidToken_returnsTrueForValidToken() {
        assertTrue(underTest.isValidToken(validToken));
    }

    @Test
    void isValidToken_returnsFalseForInvalidToken() {
        String invalidToken = validToken + "tampered";
        assertFalse(underTest.isValidToken(invalidToken));
    }

    @Test
    void getClaims_returnsClaimsForValidToken() {
        Claims claims = underTest.getClaims(validToken);
        assertEquals("testuser", claims.getSubject());
        assertEquals("test@example.com", claims.get("email", String.class));
    }

    @Test
    void getClaims_throwsExceptionForInvalidToken() {
        String invalidToken = validToken + "tampered";
        assertThrows(Exception.class, () -> underTest.getClaims(invalidToken));
    }

    private SecretKey getTestKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
    }
}
