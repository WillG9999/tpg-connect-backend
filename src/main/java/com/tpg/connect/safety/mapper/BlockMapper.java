package com.tpg.connect.safety.mapper;

import com.tpg.connect.safety.model.entity.Block;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class BlockMapper {

    public Map<String, Object> toDocument(Block block) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("blockerConnectId", block.blockerConnectId());
        doc.put("blockedConnectId", block.blockedConnectId());
        doc.put("createdAt", block.createdAt().toEpochMilli());
        return doc;
    }

    public Block fromDocument(Map<String, Object> data, String blockId) {
        return Block.builder()
                .blockId(blockId)
                .blockerConnectId(((Number) data.get("blockerConnectId")).longValue())
                .blockedConnectId(((Number) data.get("blockedConnectId")).longValue())
                .createdAt(Instant.ofEpochMilli(((Number) data.get("createdAt")).longValue()))
                .build();
    }
}
