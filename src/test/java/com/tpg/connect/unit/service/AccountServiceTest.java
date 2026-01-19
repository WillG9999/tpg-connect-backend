package com.tpg.connect.unit.service;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.settings.model.response.AccountDataResponse;
import com.tpg.connect.settings.service.AccountService;
import com.tpg.connect.settings.service.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private SettingsService settingsService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(firestore, profileRepository, settingsService);
    }

    @Test
    void downloadAccountData_returnsProfileAndSettings() {
        UserProfile profile = UserProfile.builder()
                .connectId(12345L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(profileRepository.findByConnectId(12345L)).thenReturn(Optional.of(profile));
        when(settingsService.getNotificationSettings(12345L)).thenReturn(
                new com.tpg.connect.settings.model.response.NotificationSettingsResponse(
                        true,
                        com.tpg.connect.settings.model.entity.NotificationSettings.defaults(12345L)
                )
        );
        when(settingsService.getEmailSettings(12345L)).thenReturn(
                new com.tpg.connect.settings.model.response.EmailSettingsResponse(
                        true,
                        com.tpg.connect.settings.model.entity.EmailSettings.defaults(12345L)
                )
        );
        when(settingsService.getPrivacySettings(12345L)).thenReturn(
                new com.tpg.connect.settings.model.response.PrivacySettingsResponse(
                        true,
                        com.tpg.connect.settings.model.entity.PrivacySettings.defaults(12345L)
                )
        );

        AccountDataResponse response = accountService.downloadAccountData(12345L);

        assertTrue(response.success());
        assertNotNull(response.profile());
        assertEquals("John", response.profile().firstName());
        assertNotNull(response.notificationSettings());
        assertNotNull(response.emailSettings());
        assertNotNull(response.privacySettings());
        assertNotNull(response.exportedAt());
    }

    @Test
    void downloadAccountData_returnsNullProfileWhenNotFound() {
        when(profileRepository.findByConnectId(12345L)).thenReturn(Optional.empty());
        when(settingsService.getNotificationSettings(12345L)).thenReturn(
                new com.tpg.connect.settings.model.response.NotificationSettingsResponse(
                        true,
                        com.tpg.connect.settings.model.entity.NotificationSettings.defaults(12345L)
                )
        );
        when(settingsService.getEmailSettings(12345L)).thenReturn(
                new com.tpg.connect.settings.model.response.EmailSettingsResponse(
                        true,
                        com.tpg.connect.settings.model.entity.EmailSettings.defaults(12345L)
                )
        );
        when(settingsService.getPrivacySettings(12345L)).thenReturn(
                new com.tpg.connect.settings.model.response.PrivacySettingsResponse(
                        true,
                        com.tpg.connect.settings.model.entity.PrivacySettings.defaults(12345L)
                )
        );

        AccountDataResponse response = accountService.downloadAccountData(12345L);

        assertTrue(response.success());
        assertNull(response.profile());
        assertNotNull(response.notificationSettings());
    }
}

