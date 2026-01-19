package com.tpg.connect.profile.mapper;

import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.model.request.UpdateProfileRequest;
import com.tpg.connect.profile.model.response.ProfileResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProfileMapper {

    public ProfileResponse toProfileResponse(UserProfile profile) {
        String name = profile.firstName() + " " + profile.lastName();
        int age = calculateAge(profile.dateOfBirth());

        return new ProfileResponse(
                name,
                age,
                profile.location(),
                profile.interests(),
                profile.pronouns(),
                profile.gender(),
                profile.sexuality(),
                profile.interestedIn(),
                profile.jobTitle(),
                profile.company(),
                profile.university(),
                profile.educationLevel(),
                profile.religiousBeliefs(),
                profile.hometown(),
                profile.politics(),
                profile.datingIntentions(),
                profile.relationshipType(),
                profile.height(),
                profile.ethnicity(),
                profile.children(),
                profile.familyPlans(),
                profile.pets(),
                profile.zodiacSign(),
                profile.photoUrls(),
                profile.writtenPrompts(),
                profile.fieldVisibility(),
                profile.bio()
        );
    }

    public Map<String, Object> toUpdateMap(UpdateProfileRequest request) {
        Map<String, Object> updates = new HashMap<>();
        if (request.pronouns() != null) updates.put("pronouns", request.pronouns());
        if (request.sexuality() != null) updates.put("sexuality", request.sexuality());
        if (request.interestedIn() != null) updates.put("interestedIn", request.interestedIn());
        if (request.jobTitle() != null) updates.put("jobTitle", request.jobTitle());
        if (request.company() != null) updates.put("company", request.company());
        if (request.university() != null) updates.put("university", request.university());
        if (request.educationLevel() != null) updates.put("educationLevel", request.educationLevel());
        if (request.religiousBeliefs() != null) updates.put("religiousBeliefs", request.religiousBeliefs());
        if (request.hometown() != null) updates.put("hometown", request.hometown());
        if (request.politics() != null) updates.put("politics", request.politics());
        if (request.datingIntentions() != null) updates.put("datingIntentions", request.datingIntentions());
        if (request.relationshipType() != null) updates.put("relationshipType", request.relationshipType());
        if (request.height() != null) updates.put("height", request.height());
        if (request.ethnicity() != null) updates.put("ethnicity", request.ethnicity());
        if (request.children() != null) updates.put("children", request.children());
        if (request.familyPlans() != null) updates.put("familyPlans", request.familyPlans());
        if (request.pets() != null) updates.put("pets", request.pets());
        if (request.interests() != null) updates.put("interests", request.interests());
        if (request.writtenPrompts() != null) updates.put("writtenPrompts", request.writtenPrompts());
        if (request.fieldVisibility() != null) updates.put("fieldVisibility", request.fieldVisibility());
        if (request.bio() != null) updates.put("bio", request.bio());
        return updates;
    }

    public List<String> addPhotoToList(List<String> currentPhotos, String newPhotoUrl) {
        List<String> updatedPhotos = currentPhotos != null ? new ArrayList<>(currentPhotos) : new ArrayList<>();
        updatedPhotos.add(newPhotoUrl);
        return updatedPhotos;
    }

    public List<String> removePhotoFromList(List<String> currentPhotos, String photoUrl) {
        List<String> updatedPhotos = new ArrayList<>(currentPhotos);
        updatedPhotos.remove(photoUrl);
        return updatedPhotos;
    }

    private int calculateAge(String dateOfBirth) {
        if (dateOfBirth == null) return 0;
        try {
            LocalDate birthDate = LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_LOCAL_DATE);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public UserProfile documentToProfile(Map<String, Object> data, long connectId) {
        return UserProfile.builder()
                .connectId(connectId)
                .email((String) data.get("email"))
                .firstName((String) data.get("firstName"))
                .lastName((String) data.get("lastName"))
                .dateOfBirth((String) data.get("dateOfBirth"))
                .gender((String) data.get("gender"))
                .location((String) data.get("location"))
                .interests((List<String>) data.getOrDefault("interests", List.of()))
                .pronouns((String) data.get("pronouns"))
                .sexuality((String) data.get("sexuality"))
                .interestedIn((String) data.get("interestedIn"))
                .jobTitle((String) data.get("jobTitle"))
                .company((String) data.get("company"))
                .university((String) data.get("university"))
                .educationLevel((String) data.get("educationLevel"))
                .religiousBeliefs((String) data.get("religiousBeliefs"))
                .hometown((String) data.get("hometown"))
                .politics((String) data.get("politics"))
                .datingIntentions((String) data.get("datingIntentions"))
                .relationshipType((String) data.get("relationshipType"))
                .height((String) data.get("height"))
                .ethnicity((String) data.get("ethnicity"))
                .children((String) data.get("children"))
                .familyPlans((String) data.get("familyPlans"))
                .pets((String) data.get("pets"))
                .zodiacSign((String) data.get("zodiacSign"))
                .photoUrls((List<String>) data.getOrDefault("photoUrls", List.of()))
                .writtenPrompts((List<Map<String, String>>) data.getOrDefault("writtenPrompts", List.of()))
                .fieldVisibility((Map<String, Boolean>) data.getOrDefault("fieldVisibility", Map.of()))
                .bio((String) data.get("bio"))
                .build();
    }

    public Map<String, Object> profileToDocument(UserProfile p) {
        Map<String, Object> data = new HashMap<>();
        data.put("connectId", p.connectId());
        data.put("email", p.email());
        data.put("firstName", p.firstName());
        data.put("lastName", p.lastName());
        data.put("dateOfBirth", p.dateOfBirth());
        data.put("gender", p.gender());
        data.put("location", p.location());
        data.put("interests", p.interests() != null ? p.interests() : List.of());
        data.put("pronouns", p.pronouns());
        data.put("sexuality", p.sexuality());
        data.put("interestedIn", p.interestedIn());
        data.put("jobTitle", p.jobTitle());
        data.put("company", p.company());
        data.put("university", p.university());
        data.put("educationLevel", p.educationLevel());
        data.put("religiousBeliefs", p.religiousBeliefs());
        data.put("hometown", p.hometown());
        data.put("politics", p.politics());
        data.put("datingIntentions", p.datingIntentions());
        data.put("relationshipType", p.relationshipType());
        data.put("height", p.height());
        data.put("ethnicity", p.ethnicity());
        data.put("children", p.children());
        data.put("familyPlans", p.familyPlans());
        data.put("pets", p.pets());
        data.put("zodiacSign", p.zodiacSign());
        data.put("photoUrls", p.photoUrls() != null ? p.photoUrls() : List.of());
        data.put("writtenPrompts", p.writtenPrompts() != null ? p.writtenPrompts() : List.of());
        data.put("fieldVisibility", p.fieldVisibility() != null ? p.fieldVisibility() : Map.of());
        data.put("bio", p.bio());
        return data;
    }
}
