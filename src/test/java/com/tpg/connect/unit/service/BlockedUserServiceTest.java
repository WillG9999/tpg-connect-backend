package com.tpg.connect.unit.service;

import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.safety.mapper.BlockedUserMapper;
import com.tpg.connect.safety.model.entity.BlockedUser;
import com.tpg.connect.safety.model.response.BlockedUsersResponse;
import com.tpg.connect.safety.repository.BlockedUserRepository;
import com.tpg.connect.safety.service.BlockedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockedUserServiceTest {

    @Mock
    private BlockedUserRepository blockedUserRepository;

    @Mock
    private BlockedUserMapper blockedUserMapper;

    @Mock
    private ProfileRepository profileRepository;

    private BlockedUserService blockedUserService;

    @BeforeEach
    void setUp() {
        blockedUserService = new BlockedUserService(blockedUserRepository, blockedUserMapper, profileRepository);
    }

    @Test
    void getBlockedUsers_returnsBlockedUsersWithProfileInfo() {
        BlockedUser blockedUser = BlockedUser.builder()
                .blockId("block1")
                .blockerConnectId(12345L)
                .blockedConnectId(67890L)
                .reason("Spam")
                .blockedAt(Instant.now())
                .build();

        UserProfile profile = UserProfile.builder()
                .connectId(67890L)
                .firstName("John")
                .lastName("Doe")
                .photoUrls(List.of("http://example.com/photo.jpg"))
                .build();

        when(blockedUserRepository.findByBlockerConnectId(12345L)).thenReturn(List.of(blockedUser));
        when(profileRepository.findByConnectId(67890L)).thenReturn(Optional.of(profile));

        BlockedUsersResponse response = blockedUserService.getBlockedUsers(12345L);

        assertTrue(response.success());
        assertEquals(1, response.blockedUsers().size());
        assertEquals("John", response.blockedUsers().get(0).firstName());
        assertEquals("Doe", response.blockedUsers().get(0).lastName());
    }

    @Test
    void getBlockedUsers_returnsEmptyListWhenNoBlockedUsers() {
        when(blockedUserRepository.findByBlockerConnectId(12345L)).thenReturn(List.of());

        BlockedUsersResponse response = blockedUserService.getBlockedUsers(12345L);

        assertTrue(response.success());
        assertEquals(0, response.blockedUsers().size());
        assertEquals(0, response.totalCount());
    }

    @Test
    void blockUser_blocksNewUser() {
        BlockedUser blockedUser = BlockedUser.builder()
                .blockId("block1")
                .blockerConnectId(12345L)
                .blockedConnectId(67890L)
                .reason("Harassment")
                .blockedAt(Instant.now())
                .build();

        when(blockedUserRepository.isBlocked(12345L, 67890L)).thenReturn(false);
        when(blockedUserMapper.createBlockedUser(12345L, 67890L, "Harassment")).thenReturn(blockedUser);
        when(blockedUserRepository.save(any())).thenReturn(true);

        boolean result = blockedUserService.blockUser(12345L, 67890L, "Harassment");

        assertTrue(result);
        verify(blockedUserRepository).save(any());
    }

    @Test
    void blockUser_returnsTrueIfAlreadyBlocked() {
        when(blockedUserRepository.isBlocked(12345L, 67890L)).thenReturn(true);

        boolean result = blockedUserService.blockUser(12345L, 67890L, "Harassment");

        assertTrue(result);
        verify(blockedUserRepository, never()).save(any());
    }

    @Test
    void unblockUser_unblocksExistingBlock() {
        BlockedUser blockedUser = BlockedUser.builder()
                .blockId("block1")
                .blockerConnectId(12345L)
                .blockedConnectId(67890L)
                .build();

        when(blockedUserRepository.findByBlockerAndBlocked(12345L, 67890L)).thenReturn(Optional.of(blockedUser));
        when(blockedUserRepository.delete("block1")).thenReturn(true);

        boolean result = blockedUserService.unblockUser(12345L, 67890L);

        assertTrue(result);
        verify(blockedUserRepository).delete("block1");
    }

    @Test
    void unblockUser_returnsTrueIfNotBlocked() {
        when(blockedUserRepository.findByBlockerAndBlocked(12345L, 67890L)).thenReturn(Optional.empty());

        boolean result = blockedUserService.unblockUser(12345L, 67890L);

        assertTrue(result);
        verify(blockedUserRepository, never()).delete(any());
    }

    @Test
    void isBlocked_returnsCorrectStatus() {
        when(blockedUserRepository.isBlocked(12345L, 67890L)).thenReturn(true);

        boolean result = blockedUserService.isBlocked(12345L, 67890L);

        assertTrue(result);
    }
}

