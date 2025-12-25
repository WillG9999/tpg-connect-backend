package com.tpg.connect.unit.service;

import com.tpg.connect.common.exceptions.MissingAuthorizationHeaderException;
import com.tpg.connect.common.services.authentication.JsonWebTokenAuthenticationFilter;
import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
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

    private JsonWebTokenValidatorService validatorService;
    private TestableJsonWebTokenAuthenticationFilter underTest;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    private static class TestableJsonWebTokenAuthenticationFilter extends JsonWebTokenAuthenticationFilter {
        public TestableJsonWebTokenAuthenticationFilter(JsonWebTokenValidatorService validatorService) {
            super(validatorService);
        }
        @Override
        public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws java.io.IOException, ServletException {
            super.doFilterInternal(request, response, filterChain);
        }
        @Override
        public boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
            return super.shouldNotFilter(request);
        }
    }

    @BeforeEach
    void setUp() {
        validatorService = mock(JsonWebTokenValidatorService.class);
        underTest = new TestableJsonWebTokenAuthenticationFilter(validatorService);
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
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user123");
        when(validatorService.getClaims("validtoken")).thenReturn(claims);

        underTest.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user123", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_missingHeader_throwsException() {
        when(request.getHeader(X_AUTHORISATION)).thenReturn(null);

        assertThrows(MissingAuthorizationHeaderException.class, () ->
                underTest.doFilterInternal(request, response, filterChain));
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
    void shouldNotFilter_returnsTrueForPublicEndpoints() throws ServletException {
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
    void shouldNotFilter_returnsFalseForProtectedEndpoints() throws ServletException {
        when(request.getRequestURI()).thenReturn("/api/v1/protected");
        assertFalse(underTest.shouldNotFilter(request));
    }
}
