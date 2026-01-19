package com.tpg.connect.unit.tracing;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.common.tracing.TracingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static com.tpg.connect.common.constants.HeaderConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TracingFilterTest {

    @Mock
    private JsonWebTokenValidatorService jwtValidatorService;

    private TracingFilter tracingFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        tracingFilter = new TracingFilter(jwtValidatorService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void doFilter_extractsTransactionIdFromHeader() throws Exception {
        String txnId = "test-txn-123";
        request.addHeader(X_TPG_TXN_CORRELATION_ID, txnId);

        tracingFilter.doFilter(request, response, filterChain);

        assertEquals(txnId, response.getHeader(X_TPG_TXN_CORRELATION_ID));
    }

    @Test
    void doFilter_generatesTransactionIdWhenMissing() throws Exception {
        tracingFilter.doFilter(request, response, filterChain);

        String txnId = response.getHeader(X_TPG_TXN_CORRELATION_ID);
        assertNotNull(txnId);
        assertFalse(txnId.isEmpty());
    }

    @Test
    void doFilter_extractsConnectIdFromJwt() throws Exception {
        String token = "valid-jwt-token";
        request.addHeader(X_TPG_TXN_CORRELATION_ID, "txn-123");
        request.addHeader(X_AUTHORISATION, "Bearer " + token);
        when(jwtValidatorService.extractConnectId(token)).thenReturn(12345L);

        tracingFilter.doFilter(request, response, filterChain);

        assertEquals("12345", response.getHeader(X_TPG_CONNECT_ID));
    }

    @Test
    void doFilter_handlesInvalidJwtGracefully() throws Exception {
        request.addHeader(X_TPG_TXN_CORRELATION_ID, "txn-123");
        request.addHeader(X_AUTHORISATION, "Bearer invalid-token");
        when(jwtValidatorService.extractConnectId("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        tracingFilter.doFilter(request, response, filterChain);

        assertNull(response.getHeader(X_TPG_CONNECT_ID));
    }

    @Test
    void doFilter_handlesMissingAuthorizationHeader() throws Exception {
        request.addHeader(X_TPG_TXN_CORRELATION_ID, "txn-123");

        tracingFilter.doFilter(request, response, filterChain);

        assertNull(response.getHeader(X_TPG_CONNECT_ID));
    }

    @Test
    void doFilter_clearsMdcAfterRequest() throws Exception {
        request.addHeader(X_TPG_TXN_CORRELATION_ID, "txn-123");

        tracingFilter.doFilter(request, response, filterChain);

        assertNull(MDC.get("txnId"));
        assertNull(MDC.get("connectId"));
        assertNull(MDC.get("appId"));
    }

    @Test
    void setApplicationId_setsValueInMdc() {
        TracingFilter.setApplicationId("APP-123456");

        assertEquals("APP-123456", MDC.get("appId"));

        TracingFilter.clearApplicationId();
        assertNull(MDC.get("appId"));
    }
}
