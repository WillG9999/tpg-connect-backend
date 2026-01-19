package com.tpg.connect.profile.service;

import com.tpg.connect.common.storage.service.PhotoStorageServiceApi;
import com.tpg.connect.profile.mapper.ProfileMapper;
import com.tpg.connect.profile.model.request.UpdateProfileRequest;
import com.tpg.connect.profile.model.response.ProfileResponse;
import com.tpg.connect.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PhotoStorageServiceApi photoStorageService;
    private final ProfileMapper profileMapper;

    public Optional<ProfileResponse> getProfile(long connectId) {
        log.info("Fetching profile for connectId: {}", connectId);
        return profileRepository.findByConnectId(connectId)
                .map(profileMapper::toProfileResponse);
    }

    public boolean updateProfile(long connectId, UpdateProfileRequest request) {
        log.info("Updating profile for connectId: {}", connectId);

        Map<String, Object> updates = profileMapper.toUpdateMap(request);

        if (updates.isEmpty()) {
            log.warn("No fields to update for connectId: {}", connectId);
            return true;
        }

        return profileRepository.updateProfile(connectId, updates);
    }

    public Optional<String> addPhoto(long connectId, MultipartFile photo) {
        log.info("Adding photo for connectId: {}", connectId);

        var profileOpt = profileRepository.findByConnectId(connectId);
        if (profileOpt.isEmpty()) {
            log.error("Profile not found for connectId: {}", connectId);
            return Optional.empty();
        }

        try {
            String photoUrl = photoStorageService.uploadPhoto(String.valueOf(connectId), photo);
            List<String> updatedPhotos = profileMapper.addPhotoToList(profileOpt.get().photoUrls(), photoUrl);
            profileRepository.updateProfile(connectId, Map.of("photoUrls", updatedPhotos));

            log.info("Photo added successfully for connectId: {}", connectId);
            return Optional.of(photoUrl);
        } catch (Exception e) {
            log.error("Failed to add photo for connectId: {}", connectId, e);
            return Optional.empty();
        }
    }

    public boolean removePhoto(long connectId, String photoUrl) {
        log.info("Removing photo for connectId: {}", connectId);

        var profileOpt = profileRepository.findByConnectId(connectId);
        if (profileOpt.isEmpty()) {
            log.error("Profile not found for connectId: {}", connectId);
            return false;
        }

        List<String> currentPhotos = profileOpt.get().photoUrls();
        if (currentPhotos == null || !currentPhotos.contains(photoUrl)) {
            log.warn("Photo not found in profile: {}", photoUrl);
            return false;
        }

        try {
            photoStorageService.deletePhoto(photoUrl);
            List<String> updatedPhotos = profileMapper.removePhotoFromList(currentPhotos, photoUrl);
            profileRepository.updateProfile(connectId, Map.of("photoUrls", updatedPhotos));

            log.info("Photo removed successfully for connectId: {}", connectId);
            return true;
        } catch (Exception e) {
            log.error("Failed to remove photo for connectId: {}", connectId, e);
            return false;
        }
    }
}
