package com.tpg.connect.user_registration.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class RegisteredUserMapper {

    public RegisteredUser documentToUser(DocumentSnapshot doc, long connectId) {
        return RegisteredUser.builder()
                .connectId(connectId)
                .email(doc.getString("email"))
                .password(doc.getString("password"))
                .firstName(doc.getString("firstName"))
                .lastName(doc.getString("lastName"))
                .dateOfBirth(doc.getString("dateOfBirth"))
                .gender(doc.getString("gender"))
                .location(doc.getString("location"))
                .createdAt(parseInstant(doc.getString("createdAt")))
                .build();
    }

    public Map<String, Object> userToDocument(RegisteredUser user, String applicationId) {
        Map<String, Object> data = new HashMap<>();
        data.put("connectId", String.valueOf(user.connectId()));
        data.put("email", user.email().toLowerCase());
        data.put("password", user.password());
        data.put("firstName", user.firstName());
        data.put("lastName", user.lastName());
        data.put("dateOfBirth", user.dateOfBirth());
        data.put("gender", user.gender());
        data.put("location", user.location());
        data.put("createdAt", user.createdAt().toString());
        if (applicationId != null) {
            data.put("applicationId", applicationId);
        }
        return data;
    }

    public Map<String, Object> createLookupData(long connectId, String email, String applicationId) {
        Map<String, Object> lookupData = new HashMap<>();
        lookupData.put("connectId", String.valueOf(connectId));
        lookupData.put("email", email.toLowerCase().trim());
        if (applicationId != null) {
            lookupData.put("applicationId", applicationId);
        }
        return lookupData;
    }

    private Instant parseInstant(String instantString) {
        if (instantString == null) {
            return Instant.now();
        }
        try {
            return Instant.parse(instantString);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
