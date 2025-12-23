package com.tpg.connect.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TestFirestoreService {

    @Value("${firebase.project.id}")
    private String projectId;

    @Value("${firebase.emulator.firestore.host}")
    private String firestoreHost;

    @Value("${firebase.emulator.firestore.port}")
    private String firestorePort;

    private Firestore firestore;

    public Firestore getFirestore() {
        if (firestore == null) {
            FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                    .setProjectId(projectId)
                    .setEmulatorHost(firestoreHost + ":" + firestorePort)
                    .build();
            firestore = firestoreOptions.getService();
        }
        return firestore;
    }
}