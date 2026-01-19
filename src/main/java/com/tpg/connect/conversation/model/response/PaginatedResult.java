package com.tpg.connect.conversation.model.response;

import com.tpg.connect.conversation.model.entity.Message;

import java.util.List;

public record PaginatedResult(
        List<Message> messages,
        String nextCursor,
        boolean hasMore
) {
}
