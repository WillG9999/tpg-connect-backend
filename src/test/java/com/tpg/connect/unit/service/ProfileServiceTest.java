package com.tpg.connect.unit.service;

import com.tpg.connect.common.storage.service.PhotoStorageServiceApi;
import com.tpg.connect.profile.mapper.ProfileMapper;
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.model.request.UpdateProfileRequest;
import com.tpg.connect.profile.model.response.ProfileResponse;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.profile.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PhotoStorageServiceApi photoStorageService;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private MultipartFile mockPhoto;

    private ProfileService profileService;

    private UserProfile testProfile;
    private ProfileResponse testProfileResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileService = new ProfileService(profileRepository, photoStorageService, profileMapper);

        testProfile = UserProfile.builder()
                .connectId(12345L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth("1990-01-15")
                .gender("Male")
                .location("San Francisco, CA")
                .photoUrls(new ArrayList<>(List.of("http://storage/photo1.jpg")))
                .build();

        testProfileResponse = new ProfileResponse(
                "John Doe", 35, "San Francisco, CA", List.of(), null, "Male",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                List.of("http://storage/photo1.jpg"), List.of(), Map.of(), null
        );
    }

    @Test
    void getProfile_returnsProfile_whenExists() {
        when(profileRepository.findByConnectId(12345L)).thenReturn(Optional.of(testProfile));
        when(profileMapper.toProfileResponse(testProfile)).thenReturn(testProfileResponse);

        Optional<ProfileResponse> result = profileService.getProfile(12345L);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().name());
        assertEquals("San Francisco, CA", result.get().location());
    }

    @Test
    void getProfile_returnsEmpty_whenNotFound() {
        when(profileRepository.findByConnectId(99999L)).thenReturn(Optional.empty());

        Optional<ProfileResponse> result = profileService.getProfile(99999L);

        assertFalse(result.isPresent());
    }

    @Test
    void updateProfile_returnsTrue_whenSuccessful() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "he/him", null, null, "Engineer", null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null
        );

        Map<String, Object> updates = new HashMap<>();
        updates.put("pronouns", "he/him");
        updates.put("jobTitle", "Engineer");

        when(profileMapper.toUpdateMap(request)).thenReturn(updates);
        when(profileRepository.updateProfile(eq(12345L), eq(updates))).thenReturn(true);

        boolean result = profileService.updateProfile(12345L, request);

        assertTrue(result);
        verify(profileRepository).updateProfile(12345L, updates);
    }

    @Test
    void addPhoto_uploadsAndUpdatesProfile() {
        List<String> updatedPhotos = List.of("http://storage/photo1.jpg", "http://storage/photo2.jpg");

        when(profileRepository.findByConnectId(12345L)).thenReturn(Optional.of(testProfile));
        when(photoStorageService.uploadPhoto(eq("12345"), any(MultipartFile.class)))
                .thenReturn("http://storage/photo2.jpg");
        when(profileMapper.addPhotoToList(testProfile.photoUrls(), "http://storage/photo2.jpg"))
                .thenReturn(updatedPhotos);
        when(profileRepository.updateProfile(eq(12345L), anyMap())).thenReturn(true);

        Optional<String> result = profileService.addPhoto(12345L, mockPhoto);

        assertTrue(result.isPresent());
        assertEquals("http://storage/photo2.jpg", result.get());
        verify(profileRepository).updateProfile(eq(12345L), eq(Map.of("photoUrls", updatedPhotos)));
    }

    @Test
    void addPhoto_returnsEmpty_whenProfileNotFound() {
        when(profileRepository.findByConnectId(99999L)).thenReturn(Optional.empty());

        Optional<String> result = profileService.addPhoto(99999L, mockPhoto);

        assertFalse(result.isPresent());
        verify(photoStorageService, never()).uploadPhoto(anyString(), any());
    }

    @Test
    void removePhoto_deletesAndUpdatesProfile() {
        String photoToRemove = "http://storage/photo1.jpg";
        List<String> updatedPhotos = List.of();

        when(profileRepository.findByConnectId(12345L)).thenReturn(Optional.of(testProfile));
        when(profileMapper.removePhotoFromList(testProfile.photoUrls(), photoToRemove))
                .thenReturn(updatedPhotos);
        when(profileRepository.updateProfile(eq(12345L), anyMap())).thenReturn(true);

        boolean result = profileService.removePhoto(12345L, photoToRemove);

        assertTrue(result);
        verify(photoStorageService).deletePhoto(photoToRemove);
        verify(profileRepository).updateProfile(eq(12345L), eq(Map.of("photoUrls", updatedPhotos)));
    }

    @Test
    void removePhoto_returnsFalse_whenPhotoNotInProfile() {
        when(profileRepository.findByConnectId(12345L)).thenReturn(Optional.of(testProfile));

        boolean result = profileService.removePhoto(12345L, "http://storage/nonexistent.jpg");

        assertFalse(result);
        verify(photoStorageService, never()).deletePhoto(anyString());
    }

    @Test
    void removePhoto_returnsFalse_whenProfileNotFound() {
        when(profileRepository.findByConnectId(99999L)).thenReturn(Optional.empty());

        boolean result = profileService.removePhoto(99999L, "http://storage/photo1.jpg");

        assertFalse(result);
        verify(photoStorageService, never()).deletePhoto(anyString());
    }
}

