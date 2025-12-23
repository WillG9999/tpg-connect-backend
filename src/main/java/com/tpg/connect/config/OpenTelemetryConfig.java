package com.tpg.connect.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
public class OpenTelemetryConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void logTracingStatus() {
        boolean otelAgentActive = isOpenTelemetryAgentActive();
        if (otelAgentActive) {
            String serviceName = System.getProperty("otel.services.name", "unknown");
            String exporter = System.getProperty("otel.traces.exporter", "unknown");
            log.info("OpenTelemetry tracing ENABLED - services: {}, exporter: {}", serviceName, exporter);
        } else {
            log.warn("OpenTelemetry tracing DISABLED - run with: mvn spring-boot:run");
        }
    }

    private boolean isOpenTelemetryAgentActive() {
        try {
            Class.forName("io.opentelemetry.javaagent.OpenTelemetryAgent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}