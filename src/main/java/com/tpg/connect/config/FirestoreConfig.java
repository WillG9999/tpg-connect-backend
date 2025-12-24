package com.tpg.connect.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FirestoreConfig {

    @Value("${firebase.project.id}")
    private String projectId;

    @Value("${firebase.emulator.firestore.host:#{null}}")
    private String emulatorHost;

    @Value("${firebase.emulator.firestore.port:#{null}}")
    private String emulatorPort;

    @Bean
    public Firestore firestore() {

        try {
            boolean useEmulator = emulatorHost != null && emulatorPort != null;
            FirestoreOptions.Builder optionsBuilder = FirestoreOptions.newBuilder()
                    .setProjectId(projectId);

            if (useEmulator) {
                String emulatorEndpoint = emulatorHost + ":" + emulatorPort;
                optionsBuilder.setEmulatorHost(emulatorEndpoint);
                log.info("Configuring Firestore to use emulator at:: {}", emulatorEndpoint);
            } else {
                optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault());
            }

            return optionsBuilder.build().getService();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firestore", e);
        }
    }
}