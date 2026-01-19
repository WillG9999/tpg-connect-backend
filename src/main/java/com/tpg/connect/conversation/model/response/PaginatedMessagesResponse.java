package com.tpg.connect.conversation.model.response;

import java.util.List;

public record PaginatedMessagesResponse(
        List<MessageResponse> messages,
        String nextCursor,
        boolean hasMore
) {
}
