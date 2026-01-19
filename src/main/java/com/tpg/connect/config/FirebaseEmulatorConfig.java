package com.tpg.connect.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

import java.net.Socket;

@Slf4j
@Configuration
@Profile({"dev", "test"})
public class FirebaseEmulatorConfig {

    private static Process emulatorProcess;

    @Value("${firebase.emulator.firestore.host}")
    private String firestoreHost;

    @Value("${firebase.emulator.firestore.port}")
    private String firestorePort;

    @Value("${firebase.emulator.storage.port:9199}")
    private String storagePort;

    @Value("${firebase.project.id}")
    private String projectId;

    @EventListener(ApplicationStartedEvent.class)
    public void startFirebaseEmulator() {

        log.info("Configuring Firebase Emulator - Firestore: {}:{}, Storage: {}:{}",
                firestoreHost, firestorePort, firestoreHost, storagePort);

        System.setProperty("FIRESTORE_EMULATOR_HOST", firestoreHost + ":" + firestorePort);
        System.setProperty("FIREBASE_STORAGE_EMULATOR_HOST", firestoreHost + ":" + storagePort);

        killExistingEmulators();

        try {
            log.info("Starting Firebase emulators (Firestore + Storage)...");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "firebase", "emulators:start", "--only", "firestore,storage", "--project", projectId);
            processBuilder.inheritIO();

            emulatorProcess = processBuilder.start();

            waitForEmulatorToStart();
            log.info("Firebase emulators started successfully - Firestore: {}, Storage: {}", firestorePort, storagePort);

            initializeFirebaseApp();

        } catch (Exception e) {
            log.error("Failed to start Firebase emulator: {}", e.getMessage());
        }
    }

    private void initializeFirebaseApp() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                String bucketName = projectId + ".appspot.com";

                FirebaseOptions options = FirebaseOptions.builder()
                        .setProjectId(projectId)
                        .setStorageBucket(bucketName)
                        .setCredentials(GoogleCredentials.newBuilder().build())
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized for emulator - project: {}, bucket: {}", projectId, bucketName);
            } catch (Exception e) {
                log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize Firebase", e);
            }
        }
    }

    private void killExistingEmulators() {
        try {
            log.info("Killing any existing Firebase emulators...");
            new ProcessBuilder("pkill", "-9", "-f", "cloud-firestore-emulator").start().waitFor();
            new ProcessBuilder("pkill", "-9", "-f", "firebase.*emulators").start().waitFor();

            ProcessBuilder lsof = new ProcessBuilder("lsof", "-ti", ":" + firestorePort);
            Process process = lsof.start();
            String pid = new String(process.getInputStream().readAllBytes()).trim();
            if (!pid.isEmpty())
                new ProcessBuilder("kill", "-9", pid).start().waitFor();


            Thread.sleep(1000);
        } catch (Exception e) {
            log.debug("No existing emulators to kill");
        }
    }

    @PreDestroy
    public void stopEmulator() {
        log.info("Stopping Firebase emulator...");

        try {
            if (emulatorProcess != null && emulatorProcess.isAlive()) {
                emulatorProcess.destroy();
                emulatorProcess.waitFor();
            }
        } catch (Exception e) {
            log.debug("Process already terminated");
        }

        killExistingEmulators();
        log.info("Firebase emulator stopped");
    }

    private boolean isEmulatorRunning() {
        try (Socket ignored = new Socket(firestoreHost, Integer.parseInt(firestorePort))) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForEmulatorToStart() throws InterruptedException {
        int attempts = 0;
        int maxAttempts = 30;

        while (attempts < maxAttempts && !isEmulatorRunning()) {
            Thread.sleep(500);
            attempts++;
        }

        if (!isEmulatorRunning())
            throw new RuntimeException("Firebase emulator failed to start within 15 seconds");

    }
}