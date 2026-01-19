package com.tpg.connect.conversation.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank(message = "Content is required")
        @Size(max = 1000, message = "Message cannot exceed 1000 characters")
        String content
) {
}
