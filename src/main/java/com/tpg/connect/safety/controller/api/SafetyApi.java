package com.tpg.connect.safety.controller.api;

import com.tpg.connect.safety.model.request.ReportUserRequest;
import com.tpg.connect.safety.model.response.SafetyActionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Safety", description = "Report, block, and unmatch functionality")
@RequestMapping("/v1/safety")
public interface SafetyApi {

    @Operation(summary = "Report a user", description = "Report a user for inappropriate behavior")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User reported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/users/{userId}/report")
    ResponseEntity<SafetyActionResponse> reportUser(
            @PathVariable long userId,
            @RequestBody ReportUserRequest request
    );

    @Operation(summary = "Block a user", description = "Block a user to prevent further contact")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User blocked successfully")
    })
    @PostMapping("/users/{userId}/block")
    ResponseEntity<SafetyActionResponse> blockUser(@PathVariable long userId);

    @Operation(summary = "Unblock a user", description = "Unblock a previously blocked user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User unblocked successfully")
    })
    @DeleteMapping("/users/{userId}/block")
    ResponseEntity<SafetyActionResponse> unblockUser(@PathVariable long userId);

    @Operation(summary = "Unmatch from conversation", description = "End a match and delete the conversation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unmatched successfully")
    })
    @PostMapping("/conversations/{conversationId}/unmatch")
    ResponseEntity<SafetyActionResponse> unmatch(@PathVariable String conversationId);
}
