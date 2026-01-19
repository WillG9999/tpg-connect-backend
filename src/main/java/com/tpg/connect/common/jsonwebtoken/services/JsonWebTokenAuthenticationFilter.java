package com.tpg.connect.common.jsonwebtoken.services;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenValidator;
import com.tpg.connect.common.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class JsonWebTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JsonWebTokenValidator validatorService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final Pattern BEARER_PATTERN = Pattern.compile("^Bearer\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final String TEST_TOKEN = "Token123";

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(X_AUTHORISATION);

        if (header != null) {
            String token = extractBearerToken(header);

            if (isTestEnvironment() && TEST_TOKEN.equals(token)) {
                log.info("Test token accepted in {} environment", activeProfile);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken("test-user", null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"),
                                    new SimpleGrantedAuthority("ROLE_ADMIN")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else if (token != null
                       && validatorService.isValidToken(token)
                       && validatorService.isAccessToken(token)
                       && !tokenBlacklistService.isBlacklisted(token)) {

                Claims claims = validatorService.getClaims(token);
                String subject = claims.getSubject();
                String role = claims.get("role", String.class);

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER"))
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(subject, token, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(String header) {
        var matcher = BEARER_PATTERN.matcher(header.trim());
        return matcher.matches() ? matcher.group(1).trim() : null;
    }

    private boolean isTestEnvironment() {
        return activeProfile.contains("dev") || activeProfile.contains("test");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.debug("Checking path: {}", path);

        return path.equals("/api/v1/auth/register") ||
                path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/refresh") ||
                path.equals("/api/v1/auth/send-verification-code") ||
                path.equals("/api/v1/auth/verify-email-code") ||
                path.equals("/api/v1/auth/forgot-password") ||
                path.equals("/api/v1/auth/reset-password") ||
                path.equals("/api/v1/auth/verify-reset-token") ||
                path.equals("/api/v1/application/submit") ||
                path.equals("/api/v1/application/status") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/api/actuator/health") ||
                path.equals("/error");
    }

}
