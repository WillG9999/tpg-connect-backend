package com.tpg.connect.unit.mapper;

import com.tpg.connect.profile.mapper.ProfileMapper;
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.model.request.UpdateProfileRequest;
import com.tpg.connect.profile.model.response.ProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfileMapperTest {

    private ProfileMapper profileMapper;

    @BeforeEach
    void setUp() {
        profileMapper = new ProfileMapper();
    }

    @Test
    void toProfileResponse_mapsAllFields() {
        UserProfile profile = UserProfile.builder()
                .connectId(12345L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth("1990-01-15")
                .gender("Male")
                .location("San Francisco, CA")
                .pronouns("he/him")
                .jobTitle("Engineer")
                .photoUrls(List.of("http://photo1.jpg"))
                .build();

        ProfileResponse response = profileMapper.toProfileResponse(profile);

        assertEquals("John Doe", response.name());
        assertEquals("San Francisco, CA", response.location());
        assertEquals("Male", response.gender());
        assertEquals("he/him", response.pronouns());
        assertEquals("Engineer", response.jobTitle());
        assertEquals(List.of("http://photo1.jpg"), response.photos());
        assertTrue(response.age() > 0);
    }

    @Test
    void toProfileResponse_calculatesAgeCorrectly() {
        UserProfile profile = UserProfile.builder()
                .firstName("Test")
                .lastName("User")
                .dateOfBirth("2000-01-01")
                .build();

        ProfileResponse response = profileMapper.toProfileResponse(profile);

        assertTrue(response.age() >= 25 && response.age() <= 26);
    }

    @Test
    void toProfileResponse_handlesNullDateOfBirth() {
        UserProfile profile = UserProfile.builder()
                .firstName("Test")
                .lastName("User")
                .dateOfBirth(null)
                .build();

        ProfileResponse response = profileMapper.toProfileResponse(profile);

        assertEquals(0, response.age());
    }

    @Test
    void toUpdateMap_includesOnlyNonNullFields() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "he/him", null, null, "Engineer", null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null
        );

        Map<String, Object> updates = profileMapper.toUpdateMap(request);

        assertEquals(2, updates.size());
        assertEquals("he/him", updates.get("pronouns"));
        assertEquals("Engineer", updates.get("jobTitle"));
        assertFalse(updates.containsKey("sexuality"));
    }

    @Test
    void toUpdateMap_returnsEmptyMapWhenAllNull() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null
        );

        Map<String, Object> updates = profileMapper.toUpdateMap(request);

        assertTrue(updates.isEmpty());
    }

    @Test
    void addPhotoToList_addsToExistingList() {
        List<String> currentPhotos = List.of("http://photo1.jpg");

        List<String> result = profileMapper.addPhotoToList(currentPhotos, "http://photo2.jpg");

        assertEquals(2, result.size());
        assertTrue(result.contains("http://photo1.jpg"));
        assertTrue(result.contains("http://photo2.jpg"));
    }

    @Test
    void addPhotoToList_handlesNullList() {
        List<String> result = profileMapper.addPhotoToList(null, "http://photo1.jpg");

        assertEquals(1, result.size());
        assertEquals("http://photo1.jpg", result.get(0));
    }

    @Test
    void removePhotoFromList_removesPhoto() {
        List<String> currentPhotos = List.of("http://photo1.jpg", "http://photo2.jpg");

        List<String> result = profileMapper.removePhotoFromList(currentPhotos, "http://photo1.jpg");

        assertEquals(1, result.size());
        assertEquals("http://photo2.jpg", result.get(0));
    }

    @Test
    void documentToProfile_mapsAllFields() {
        Map<String, Object> data = Map.of(
                "email", "test@example.com",
                "firstName", "John",
                "lastName", "Doe",
                "gender", "Male",
                "location", "NYC"
        );

        UserProfile profile = profileMapper.documentToProfile(data, 12345L);

        assertEquals(12345L, profile.connectId());
        assertEquals("test@example.com", profile.email());
        assertEquals("John", profile.firstName());
        assertEquals("Doe", profile.lastName());
        assertEquals("Male", profile.gender());
        assertEquals("NYC", profile.location());
    }
}

