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
            String serviceName = System.getProperty("otel.service.name", "unknown");
            String exporter = System.getProperty("otel.traces.exporter", "unknown");
            String propagators = System.getProperty("otel.propagators", "unknown");
            log.info("OpenTelemetry tracing ENABLED - service: {}, exporter: {}, propagators: {}",
                    serviceName, exporter, propagators);
        } else {
            log.info("OpenTelemetry agent not loaded - tracing via MDC only");
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