package com.tpg.connect.common.ratelimit;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final RateLimitServiceApi rateLimitService;
    private final JsonWebTokenValidatorService jwtValidatorService;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isTestEnvironment()) {
            filterChain.doFilter(request, response);
            return;
        }

        String identifier = extractIdentifier(request);
        RateLimitPlan plan = determinePlan(request);

        RateLimitResult result = rateLimitService.tryConsume(identifier, plan);

        response.setHeader(RATE_LIMIT_HEADER, String.valueOf(result.getLimit()));
        response.setHeader(RATE_LIMIT_REMAINING_HEADER, String.valueOf(result.getRemaining()));

        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded for identifier: {}, plan: {}", identifier, plan);
            response.setHeader(RETRY_AFTER_HEADER, String.valueOf(result.getRetryAfterSeconds()));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            response.setContentType("application/json");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractIdentifier(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader(X_AUTHORISATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Long connectId = jwtValidatorService.extractConnectId(token);
                if (connectId != null) {
                    return "user:" + connectId;
                }
            }
        } catch (Exception e) {
            log.trace("Could not extract connectId for rate limiting");
        }

        return "ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private RateLimitPlan determinePlan(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.contains("/auth/login") || path.contains("/auth/register") ||
            path.contains("/auth/forgot-password") || path.contains("/application/submit")) {
            return RateLimitPlan.AUTH;
        }

        if (path.contains("/messages") || path.contains("/conversations")) {
            return RateLimitPlan.MESSAGING;
        }

        if (path.contains("/photos") && "POST".equalsIgnoreCase(method)) {
            return RateLimitPlan.PHOTO_UPLOAD;
        }

        if (path.contains("/report") || path.contains("/block")) {
            return RateLimitPlan.REPORTS;
        }

        return RateLimitPlan.API_STANDARD;
    }

    private boolean isTestEnvironment() {
        return activeProfile != null && (activeProfile.contains("test") || activeProfile.contains("dev"));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/actuator") ||
               path.contains("/swagger") ||
               path.contains("/api-docs") ||
               path.contains("/health");
    }
}

