package com.tpg.connect.safety.service;

import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.safety.mapper.BlockedUserMapper;
import com.tpg.connect.safety.model.entity.BlockedUser;
import com.tpg.connect.safety.model.response.BlockedUsersResponse;
import com.tpg.connect.safety.repository.BlockedUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockedUserService {

    private final BlockedUserRepository blockedUserRepository;
    private final BlockedUserMapper blockedUserMapper;
    private final ProfileRepository profileRepository;

    public BlockedUsersResponse getBlockedUsers(long connectId) {
        log.info("Getting blocked users for connectId: {}", connectId);

        List<BlockedUser> blockedUsers = blockedUserRepository.findByBlockerConnectId(connectId);
        List<BlockedUsersResponse.BlockedUserInfo> blockedUserInfos = new ArrayList<>();

        for (BlockedUser blockedUser : blockedUsers) {
            Optional<UserProfile> profileOpt = profileRepository.findByConnectId(blockedUser.blockedConnectId());
            String firstName = profileOpt.map(UserProfile::firstName).orElse("Unknown");
            String lastName = profileOpt.map(UserProfile::lastName).orElse("User");
            String photoUrl = profileOpt
                    .map(UserProfile::photoUrls)
                    .filter(urls -> urls != null && !urls.isEmpty())
                    .map(urls -> urls.get(0))
                    .orElse(null);

            blockedUserInfos.add(BlockedUsersResponse.BlockedUserInfo.fromBlockedUser(
                    blockedUser, firstName, lastName, photoUrl
            ));
        }

        return new BlockedUsersResponse(true, blockedUserInfos, blockedUserInfos.size());
    }

    public boolean blockUser(long blockerConnectId, long blockedConnectId, String reason) {
        log.info("Blocking user: {} blocks {}", blockerConnectId, blockedConnectId);

        if (blockedUserRepository.isBlocked(blockerConnectId, blockedConnectId)) {
            log.info("User already blocked");
            return true;
        }

        BlockedUser blockedUser = blockedUserMapper.createBlockedUser(blockerConnectId, blockedConnectId, reason);
        return blockedUserRepository.save(blockedUser);
    }

    public boolean unblockUser(long blockerConnectId, long blockedConnectId) {
        log.info("Unblocking user: {} unblocks {}", blockerConnectId, blockedConnectId);

        Optional<BlockedUser> blockedUserOpt = blockedUserRepository.findByBlockerAndBlocked(blockerConnectId, blockedConnectId);
        if (blockedUserOpt.isEmpty()) {
            log.info("User not blocked");
            return true;
        }

        return blockedUserRepository.delete(blockedUserOpt.get().blockId());
    }

    public boolean isBlocked(long blockerConnectId, long blockedConnectId) {
        return blockedUserRepository.isBlocked(blockerConnectId, blockedConnectId);
    }
}

