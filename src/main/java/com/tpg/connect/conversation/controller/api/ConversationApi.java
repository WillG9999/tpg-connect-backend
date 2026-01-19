package com.tpg.connect.conversation.controller.api;

import com.tpg.connect.conversation.model.request.SendMessageRequest;
import com.tpg.connect.conversation.model.response.ConversationResponse;
import com.tpg.connect.conversation.model.response.MessageResponse;
import com.tpg.connect.conversation.model.response.PaginatedMessagesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Conversations", description = "Conversation and messaging endpoints")
@RequestMapping("/v1/conversations")
public interface ConversationApi {

    @Operation(summary = "Get user's conversations", description = "Retrieves all conversations for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversations retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    ResponseEntity<List<ConversationResponse>> getConversations();

    @Operation(summary = "Get conversation messages", description = "Retrieves paginated messages for a conversation with cursor-based pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Conversation not found"),
            @ApiResponse(responseCode = "403", description = "User not a participant")
    })
    @GetMapping("/{conversationId}/messages")
    ResponseEntity<PaginatedMessagesResponse> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String cursor
    );

    @Operation(summary = "Send a message", description = "Sends a message in a conversation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "404", description = "Conversation not found"),
            @ApiResponse(responseCode = "403", description = "User not a participant")
    })
    @PostMapping("/{conversationId}/messages")
    ResponseEntity<MessageResponse> sendMessage(
            @PathVariable String conversationId,
            @RequestBody SendMessageRequest request
    );

    @Operation(summary = "Mark messages as read", description = "Marks all unread messages in a conversation as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages marked as read"),
            @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    @PostMapping("/{conversationId}/read")
    ResponseEntity<Void> markAsRead(@PathVariable String conversationId);

    @Operation(summary = "Create or get conversation", description = "Creates a new conversation or returns existing one between two users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation retrieved/created successfully")
    })
    @PostMapping("/with/{otherUserId}")
    ResponseEntity<ConversationResponse> getOrCreateConversation(@PathVariable Long otherUserId);
}
