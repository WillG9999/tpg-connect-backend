package com.tpg.connect.profile.service;

import com.tpg.connect.profile.mapper.PreferencesMapper;
import com.tpg.connect.profile.model.entity.UserPreferences;
import com.tpg.connect.profile.model.request.UpdatePreferencesRequest;
import com.tpg.connect.profile.model.response.PreferencesResponse;
import com.tpg.connect.profile.repository.PreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreferencesService {

    private final PreferencesRepository preferencesRepository;
    private final PreferencesMapper preferencesMapper;

    public PreferencesResponse getPreferences(long connectId) {
        log.info("Getting preferences for connectId: {}", connectId);

        UserPreferences preferences = preferencesRepository.findByConnectId(connectId)
                .orElse(UserPreferences.defaultPreferences(connectId));

        return PreferencesResponse.success(preferences);
    }

    public PreferencesResponse updatePreferences(long connectId, UpdatePreferencesRequest request) {
        log.info("Updating preferences for connectId: {}", connectId);

        UserPreferences current = preferencesRepository.findByConnectId(connectId)
                .orElse(UserPreferences.defaultPreferences(connectId));

        UserPreferences updated = preferencesMapper.applyUpdate(current, request);

        preferencesRepository.save(updated);

        return PreferencesResponse.success(updated);
    }

    public boolean resetPreferences(long connectId) {
        log.info("Resetting preferences for connectId: {}", connectId);

        UserPreferences defaults = UserPreferences.defaultPreferences(connectId);
        return preferencesRepository.save(defaults);
    }
}

