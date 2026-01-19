package com.tpg.connect.unit.ratelimit;

import com.tpg.connect.common.ratelimit.RateLimitFilter;
import com.tpg.connect.common.ratelimit.RateLimitPlan;
import com.tpg.connect.common.ratelimit.RateLimitResult;
import com.tpg.connect.common.ratelimit.RateLimitServiceApi;
import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitServiceApi rateLimitService;

    @Mock
    private JsonWebTokenValidatorService jwtValidatorService;

    private RateLimitFilter rateLimitFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(rateLimitService, jwtValidatorService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void doFilter_allowsRequestWhenUnderLimit() throws Exception {
        request.setRequestURI("/api/v1/profile");
        when(rateLimitService.tryConsume(anyString(), any(RateLimitPlan.class)))
                .thenReturn(RateLimitResult.allowed(100, 99));

        rateLimitFilter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals("100", response.getHeader("X-RateLimit-Limit"));
        assertEquals("99", response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    void doFilter_blocksRequestWhenOverLimit() throws Exception {
        request.setRequestURI("/api/v1/profile");
        when(rateLimitService.tryConsume(anyString(), any(RateLimitPlan.class)))
                .thenReturn(RateLimitResult.blocked(100, 0, 60));

        rateLimitFilter.doFilter(request, response, filterChain);

        assertEquals(429, response.getStatus());
        assertEquals("60", response.getHeader("Retry-After"));
        assertTrue(response.getContentAsString().contains("Rate limit exceeded"));
    }

    @Test
    void doFilter_usesConnectIdWhenAuthenticated() throws Exception {
        request.setRequestURI("/api/v1/profile");
        request.addHeader(X_AUTHORISATION, "Bearer valid-token");
        when(jwtValidatorService.extractConnectId("valid-token")).thenReturn(12345L);
        when(rateLimitService.tryConsume(eq("user:12345"), any(RateLimitPlan.class)))
                .thenReturn(RateLimitResult.allowed(100, 99));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService).tryConsume(eq("user:12345"), any(RateLimitPlan.class));
    }

    @Test
    void doFilter_usesIpWhenNotAuthenticated() throws Exception {
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.100");
        when(rateLimitService.tryConsume(eq("ip:192.168.1.100"), any(RateLimitPlan.class)))
                .thenReturn(RateLimitResult.allowed(10, 9));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService).tryConsume(eq("ip:192.168.1.100"), any(RateLimitPlan.class));
    }

    @Test
    void doFilter_usesAuthPlanForLoginEndpoint() throws Exception {
        request.setRequestURI("/api/v1/auth/login");
        when(rateLimitService.tryConsume(anyString(), eq(RateLimitPlan.AUTH)))
                .thenReturn(RateLimitResult.allowed(10, 9));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService).tryConsume(anyString(), eq(RateLimitPlan.AUTH));
    }

    @Test
    void doFilter_usesMessagingPlanForConversations() throws Exception {
        request.setRequestURI("/api/v1/conversations/123/messages");
        when(rateLimitService.tryConsume(anyString(), eq(RateLimitPlan.MESSAGING)))
                .thenReturn(RateLimitResult.allowed(30, 29));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService).tryConsume(anyString(), eq(RateLimitPlan.MESSAGING));
    }

    @Test
    void doFilter_usesPhotoUploadPlanForPhotoPost() throws Exception {
        request.setRequestURI("/api/v1/profile/photos");
        request.setMethod("POST");
        when(rateLimitService.tryConsume(anyString(), eq(RateLimitPlan.PHOTO_UPLOAD)))
                .thenReturn(RateLimitResult.allowed(10, 9));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService).tryConsume(anyString(), eq(RateLimitPlan.PHOTO_UPLOAD));
    }

    @Test
    void doFilter_usesReportsPlanForBlockEndpoint() throws Exception {
        request.setRequestURI("/api/v1/safety/block");
        when(rateLimitService.tryConsume(anyString(), eq(RateLimitPlan.REPORTS)))
                .thenReturn(RateLimitResult.allowed(5, 4));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService).tryConsume(anyString(), eq(RateLimitPlan.REPORTS));
    }

    @Test
    void doFilter_skipsActuatorEndpoints() throws Exception {
        request.setRequestURI("/api/actuator/health");

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService, never()).tryConsume(anyString(), any(RateLimitPlan.class));
    }

    @Test
    void doFilter_skipsSwaggerEndpoints() throws Exception {
        request.setRequestURI("/api/swagger-ui.html");

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService, never()).tryConsume(anyString(), any(RateLimitPlan.class));
    }

    @Test
    void doFilter_usesXForwardedForHeader() throws Exception {
        request.setRequestURI("/api/v1/auth/login");
        request.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1");
        when(rateLimitService.tryConsume(eq("ip:10.0.0.1"), any(RateLimitPlan.class)))
                .thenReturn(RateLimitResult.allowed(10, 9));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService).tryConsume(eq("ip:10.0.0.1"), any(RateLimitPlan.class));
    }
}

