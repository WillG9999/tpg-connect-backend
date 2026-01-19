package com.tpg.connect.common.tracing;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.tpg.connect.common.constants.HeaderConstants.*;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class TracingFilter extends OncePerRequestFilter {

    private static final String TXN_ID_MDC_KEY = "txnId";
    private static final String CONNECT_ID_MDC_KEY = "connectId";
    private static final String APP_ID_MDC_KEY = "appId";

    private final JsonWebTokenValidatorService jwtValidatorService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String transactionId = extractOrGenerateTransactionId(request);
        Long connectId = extractConnectId(request);

        MDC.put(TXN_ID_MDC_KEY, transactionId);
        if (connectId != null) {
            MDC.put(CONNECT_ID_MDC_KEY, String.valueOf(connectId));
        }

        BaggageBuilder baggageBuilder = Baggage.builder()
                .put(X_TPG_TXN_CORRELATION_ID, transactionId);

        if (connectId != null) {
            baggageBuilder.put(X_TPG_CONNECT_ID, String.valueOf(connectId));
        }

        Baggage baggage = baggageBuilder.build();
        Context contextWithBaggage = Context.current().with(baggage);

        try (Scope scope = contextWithBaggage.makeCurrent()) {
            addSpanAttributes(transactionId, connectId);

            response.setHeader(X_TPG_TXN_CORRELATION_ID, transactionId);
            if (connectId != null) {
                response.setHeader(X_TPG_CONNECT_ID, String.valueOf(connectId));
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TXN_ID_MDC_KEY);
            MDC.remove(CONNECT_ID_MDC_KEY);
            MDC.remove(APP_ID_MDC_KEY);
        }
    }

    private String extractOrGenerateTransactionId(HttpServletRequest request) {
        String transactionId = request.getHeader(X_TPG_TXN_CORRELATION_ID);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }
        return transactionId;
    }

    private Long extractConnectId(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader(X_AUTHORISATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtValidatorService.extractConnectId(token);
            }
        } catch (Exception e) {
            log.trace("Could not extract connectId from token");
        }
        return null;
    }

    private void addSpanAttributes(String transactionId, Long connectId) {
        try {
            Span currentSpan = Span.current();
            if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
                currentSpan.setAttribute(X_TPG_TXN_CORRELATION_ID, transactionId);
                if (connectId != null) {
                    currentSpan.setAttribute(X_TPG_CONNECT_ID, connectId);
                }
            }
        } catch (Exception e) {
            log.trace("OpenTelemetry agent not loaded");
        }
    }

    public static void setApplicationId(String applicationId) {
        if (applicationId != null) {
            MDC.put(APP_ID_MDC_KEY, applicationId);
            try {
                Span currentSpan = Span.current();
                if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
                    currentSpan.setAttribute(X_TPG_APPLICATION_ID, applicationId);
                }
            } catch (Exception e) {
                // OpenTelemetry agent not loaded
            }
        }
    }

    public static void clearApplicationId() {
        MDC.remove(APP_ID_MDC_KEY);
    }

    public static String getCurrentTransactionId() {
        return Baggage.current().getEntryValue(X_TPG_TXN_CORRELATION_ID);
    }

    public static Long getCurrentConnectId() {
        String value = Baggage.current().getEntryValue(X_TPG_CONNECT_ID);
        return value != null ? Long.parseLong(value) : null;
    }

    public static String getCurrentApplicationId() {
        return Baggage.current().getEntryValue(X_TPG_APPLICATION_ID);
    }
}

