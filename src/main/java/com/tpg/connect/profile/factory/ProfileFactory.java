package com.tpg.connect.profile.factory;

import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.profile.model.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProfileFactory {

    public UserProfile createFromApplication(Application application, long connectId) {
        return UserProfile.builder()
                .connectId(connectId)
                .email(application.email())
                .firstName(application.firstName())
                .lastName(application.lastName())
                .dateOfBirth(application.dateOfBirth())
                .gender(application.gender())
                .location(application.location())
                .photoUrls(application.photoUrls())
                .interests(List.of())
                .writtenPrompts(List.of())
                .fieldVisibility(Map.of())
                .build();
    }
}
