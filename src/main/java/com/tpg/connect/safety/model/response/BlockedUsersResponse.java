package com.tpg.connect.safety.model.response;

import com.tpg.connect.safety.model.entity.BlockedUser;

import java.util.List;

public record BlockedUsersResponse(
        boolean success,
        List<BlockedUserInfo> blockedUsers,
        int totalCount
) {
    public record BlockedUserInfo(
            String blockId,
            long blockedConnectId,
            String firstName,
            String lastName,
            String photoUrl,
            String reason,
            long blockedAt
    ) {
        public static BlockedUserInfo fromBlockedUser(BlockedUser blockedUser, String firstName, String lastName, String photoUrl) {
            return new BlockedUserInfo(
                    blockedUser.blockId(),
                    blockedUser.blockedConnectId(),
                    firstName,
                    lastName,
                    photoUrl,
                    blockedUser.reason(),
                    blockedUser.blockedAt().toEpochMilli()
            );
        }
    }
}

