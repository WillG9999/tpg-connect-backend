package com.tpg.connect.profile.controller;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.profile.controller.api.ProfileApi;
import com.tpg.connect.profile.model.request.UpdateProfileRequest;
import com.tpg.connect.profile.model.response.ProfileResponse;
import com.tpg.connect.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProfileController implements ProfileApi {

    private final ProfileService profileService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<ProfileResponse> getProfile() {
        long connectId = extractConnectId();
        log.info("Get profile request for connectId: {}", connectId);

        return profileService.getProfile(connectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> updateProfile(UpdateProfileRequest request) {
        long connectId = extractConnectId();
        log.info("Update profile request for connectId: {}", connectId);

        boolean updated = profileService.updateProfile(connectId, request);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Map<String, String>> addPhoto(MultipartFile photo) {
        long connectId = extractConnectId();
        log.info("Add photo request for connectId: {}", connectId);

        return profileService.addPhoto(connectId, photo)
                .map(url -> ResponseEntity.ok(Map.of("photoUrl", url)))
                .orElse(ResponseEntity.badRequest().build());
    }

    @Override
    public ResponseEntity<Void> removePhoto(String photoUrl) {
        long connectId = extractConnectId();
        log.info("Remove photo request for connectId: {}", connectId);

        boolean removed = profileService.removePhoto(connectId, photoUrl);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    private long extractConnectId() {
        String authHeader = httpServletRequest.getHeader(X_AUTHORISATION);
        String token = authHeader.replace("Bearer ", "");
        return jwtValidatorService.extractConnectId(token);
    }
}
