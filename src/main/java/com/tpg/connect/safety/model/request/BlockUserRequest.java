package com.tpg.connect.safety.model.request;

public record BlockUserRequest(
        long userIdToBlock,
        String reason
) {
}

