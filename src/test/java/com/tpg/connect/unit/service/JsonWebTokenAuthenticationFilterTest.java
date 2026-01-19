package com.tpg.connect.unit.service;

import com.tpg.connect.common.jsonwebtoken.services.JsonWebTokenAuthenticationFilter;
import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenValidator;
import com.tpg.connect.common.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class JsonWebTokenAuthenticationFilterTest {

    private JsonWebTokenValidator validatorService;
    private TokenBlacklistService tokenBlacklistService;
    private TestableJsonWebTokenAuthenticationFilter underTest;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    private static class TestableJsonWebTokenAuthenticationFilter extends JsonWebTokenAuthenticationFilter {
        public TestableJsonWebTokenAuthenticationFilter(JsonWebTokenValidator validatorService, TokenBlacklistService tokenBlacklistService) {
            super(validatorService, tokenBlacklistService);
        }
        @Override
        public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws java.io.IOException, ServletException {
            super.doFilterInternal(request, response, filterChain);
        }
        @Override
        public boolean shouldNotFilter(HttpServletRequest request) {
            return super.shouldNotFilter(request);
        }
    }

    @BeforeEach
    void setUp() {
        validatorService = mock(JsonWebTokenValidator.class);
        tokenBlacklistService = mock(TokenBlacklistService.class);
        underTest = new TestableJsonWebTokenAuthenticationFilter(validatorService, tokenBlacklistService);
        ReflectionTestUtils.setField(underTest, "activeProfile", "test");
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        when(request.getHeader(X_AUTHORISATION)).thenReturn("Bearer validtoken");
        when(validatorService.isValidToken("validtoken")).thenReturn(true);
        when(validatorService.isAccessToken("validtoken")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("validtoken")).thenReturn(false);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user123");
        when(claims.get("role", String.class)).thenReturn("USER");
        when(validatorService.getClaims("validtoken")).thenReturn(claims);

        underTest.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user123", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_missingHeader_doesNotSetAuthentication() throws Exception {
        when(request.getHeader(X_AUTHORISATION)).thenReturn(null);

        underTest.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_doesNotSetAuthentication() throws Exception {
        when(request.getHeader(X_AUTHORISATION)).thenReturn("Bearer invalidtoken");
        when(validatorService.isValidToken("invalidtoken")).thenReturn(false);

        underTest.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_blacklistedToken_doesNotSetAuthentication() throws Exception {
        when(request.getHeader(X_AUTHORISATION)).thenReturn("Bearer blacklisted");
        when(validatorService.isValidToken("blacklisted")).thenReturn(true);
        when(validatorService.isAccessToken("blacklisted")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("blacklisted")).thenReturn(true);

        underTest.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_returnsTrueForPublicEndpoints() {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");
        assertTrue(underTest.shouldNotFilter(request));
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        assertTrue(underTest.shouldNotFilter(request));
        when(request.getRequestURI()).thenReturn("/api-docs");
        assertTrue(underTest.shouldNotFilter(request));
        when(request.getRequestURI()).thenReturn("/v3/api-docs");
        assertTrue(underTest.shouldNotFilter(request));
        when(request.getRequestURI()).thenReturn("/error");
        assertTrue(underTest.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_returnsFalseForProtectedEndpoints() {
        when(request.getRequestURI()).thenReturn("/api/v1/protected");
        assertFalse(underTest.shouldNotFilter(request));
    }
}
