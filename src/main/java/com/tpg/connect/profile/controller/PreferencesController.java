package com.tpg.connect.profile.controller;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.profile.controller.api.PreferencesApi;
import com.tpg.connect.profile.model.entity.UserPreferences;
import com.tpg.connect.profile.model.request.UpdatePreferencesRequest;
import com.tpg.connect.profile.model.response.PreferencesResponse;
import com.tpg.connect.profile.service.PreferencesService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PreferencesController implements PreferencesApi {

    private final PreferencesService preferencesService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<PreferencesResponse> getPreferences() {
        long connectId = extractConnectId();
        log.info("Getting preferences for connectId: {}", connectId);
        return ResponseEntity.ok(preferencesService.getPreferences(connectId));
    }

    @Override
    public ResponseEntity<PreferencesResponse> updatePreferences(UpdatePreferencesRequest request) {
        long connectId = extractConnectId();
        log.info("Updating preferences for connectId: {}", connectId);
        return ResponseEntity.ok(preferencesService.updatePreferences(connectId, request));
    }

    @Override
    public ResponseEntity<PreferencesResponse> resetPreferences() {
        long connectId = extractConnectId();
        log.info("Resetting preferences for connectId: {}", connectId);
        preferencesService.resetPreferences(connectId);
        return ResponseEntity.ok(PreferencesResponse.success(UserPreferences.defaultPreferences(connectId)));
    }

    private long extractConnectId() {
        String authHeader = httpServletRequest.getHeader(X_AUTHORISATION);
        String token = authHeader.replace("Bearer ", "");
        return jwtValidatorService.extractConnectId(token);
    }
}

