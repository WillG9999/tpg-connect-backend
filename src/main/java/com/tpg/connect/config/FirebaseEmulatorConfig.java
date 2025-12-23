package com.tpg.connect.config;

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

    @EventListener(ApplicationStartedEvent.class)
    public void startFirebaseEmulator() {

        log.info("Configuring Firebase Emulator at {}:{}", firestoreHost, firestorePort);
        System.setProperty("FIRESTORE_EMULATOR_HOST", firestoreHost + ":" + firestorePort);
        killExistingEmulators();

        try {
            log.info("Starting Firebase Firestore emulator...");
            ProcessBuilder processBuilder = new ProcessBuilder("firebase", "emulators:start", "--only", "firestore");
            processBuilder.inheritIO();

            emulatorProcess = processBuilder.start();

            waitForEmulatorToStart();
            log.info("Firebase Firestore emulator started successfully on port {}", firestorePort);

        } catch (Exception e) {
            log.error("Failed to start Firebase emulator: {}", e.getMessage());
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