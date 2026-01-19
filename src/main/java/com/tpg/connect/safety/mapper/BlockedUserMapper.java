package com.tpg.connect.safety.mapper;

import com.tpg.connect.safety.model.entity.BlockedUser;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class BlockedUserMapper {

    public Map<String, Object> toDocument(BlockedUser blockedUser) {
        Map<String, Object> data = new HashMap<>();
        data.put("blockId", blockedUser.blockId());
        data.put("blockerConnectId", blockedUser.blockerConnectId());
        data.put("blockedConnectId", blockedUser.blockedConnectId());
        data.put("reason", blockedUser.reason());
        data.put("blockedAt", blockedUser.blockedAt() != null ? blockedUser.blockedAt().toEpochMilli() : null);
        return data;
    }

    public BlockedUser toEntity(Map<String, Object> data, String blockId) {
        return BlockedUser.builder()
                .blockId(blockId)
                .blockerConnectId(getLong(data, "blockerConnectId"))
                .blockedConnectId(getLong(data, "blockedConnectId"))
                .reason(getString(data, "reason"))
                .blockedAt(data.get("blockedAt") != null ? Instant.ofEpochMilli(((Number) data.get("blockedAt")).longValue()) : Instant.now())
                .build();
    }

    public BlockedUser createBlockedUser(long blockerConnectId, long blockedConnectId, String reason) {
        return BlockedUser.builder()
                .blockId(UUID.randomUUID().toString())
                .blockerConnectId(blockerConnectId)
                .blockedConnectId(blockedConnectId)
                .reason(reason)
                .blockedAt(Instant.now())
                .build();
    }

    private long getLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? ((Number) value).longValue() : 0L;
    }

    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? (String) value : null;
    }
}

