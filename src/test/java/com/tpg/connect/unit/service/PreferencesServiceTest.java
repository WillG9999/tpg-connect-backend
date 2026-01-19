package com.tpg.connect.unit.service;

import com.tpg.connect.profile.mapper.PreferencesMapper;
import com.tpg.connect.profile.model.entity.UserPreferences;
import com.tpg.connect.profile.model.request.UpdatePreferencesRequest;
import com.tpg.connect.profile.model.response.PreferencesResponse;
import com.tpg.connect.profile.repository.PreferencesRepository;
import com.tpg.connect.profile.service.PreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreferencesServiceTest {

    @Mock
    private PreferencesRepository preferencesRepository;

    private PreferencesMapper preferencesMapper;
    private PreferencesService preferencesService;

    @BeforeEach
    void setUp() {
        preferencesMapper = new PreferencesMapper();
        preferencesService = new PreferencesService(preferencesRepository, preferencesMapper);
    }

    @Test
    void getPreferences_returnsExistingPreferences() {
        UserPreferences existing = UserPreferences.builder()
                .connectId(12345L)
                .preferredGender("Women")
                .minAge(25)
                .maxAge(35)
                .maxDistance(30)
                .distanceUnit("miles")
                .showVerifiedOnly(true)
                .build();

        when(preferencesRepository.findByConnectId(12345L)).thenReturn(Optional.of(existing));

        PreferencesResponse response = preferencesService.getPreferences(12345L);

        assertTrue(response.success());
        assertEquals("Women", response.preferences().preferredGender());
        assertEquals(25, response.preferences().minAge());
        assertEquals(35, response.preferences().maxAge());
    }

    @Test
    void getPreferences_returnsDefaultsWhenNoneExist() {
        when(preferencesRepository.findByConnectId(12345L)).thenReturn(Optional.empty());

        PreferencesResponse response = preferencesService.getPreferences(12345L);

        assertTrue(response.success());
        assertEquals("Everyone", response.preferences().preferredGender());
        assertEquals(18, response.preferences().minAge());
        assertEquals(50, response.preferences().maxAge());
    }

    @Test
    void updatePreferences_updatesOnlyProvidedFields() {
        UserPreferences existing = UserPreferences.builder()
                .connectId(12345L)
                .preferredGender("Everyone")
                .minAge(18)
                .maxAge(50)
                .maxDistance(50)
                .distanceUnit("miles")
                .showVerifiedOnly(false)
                .build();

        UpdatePreferencesRequest request = new UpdatePreferencesRequest(
                "Women",
                25,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(preferencesRepository.findByConnectId(12345L)).thenReturn(Optional.of(existing));
        when(preferencesRepository.save(any())).thenReturn(true);

        PreferencesResponse response = preferencesService.updatePreferences(12345L, request);

        assertTrue(response.success());
        assertEquals("Women", response.preferences().preferredGender());
        assertEquals(25, response.preferences().minAge());
        assertEquals(50, response.preferences().maxAge());
        verify(preferencesRepository).save(any());
    }

    @Test
    void updatePreferences_createsNewWhenNoneExist() {
        UpdatePreferencesRequest request = new UpdatePreferencesRequest(
                "Men",
                22,
                40,
                25,
                "km",
                "Serious",
                null,
                null,
                null,
                null,
                true
        );

        when(preferencesRepository.findByConnectId(12345L)).thenReturn(Optional.empty());
        when(preferencesRepository.save(any())).thenReturn(true);

        PreferencesResponse response = preferencesService.updatePreferences(12345L, request);

        assertTrue(response.success());
        assertEquals("Men", response.preferences().preferredGender());
        assertEquals(22, response.preferences().minAge());
        assertEquals(40, response.preferences().maxAge());
        assertEquals("km", response.preferences().distanceUnit());
        assertTrue(response.preferences().showVerifiedOnly());
    }

    @Test
    void resetPreferences_savesDefaults() {
        when(preferencesRepository.save(any())).thenReturn(true);

        boolean result = preferencesService.resetPreferences(12345L);

        assertTrue(result);
        verify(preferencesRepository).save(argThat(prefs ->
                prefs.preferredGender().equals("Everyone") &&
                prefs.minAge() == 18 &&
                prefs.maxAge() == 50
        ));
    }
}

