package com.tpg.connect.safety.controller.api;

import com.tpg.connect.safety.model.request.BlockUserRequest;
import com.tpg.connect.safety.model.request.UnblockUserRequest;
import com.tpg.connect.safety.model.response.BlockedUsersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Safety", description = "Block and safety management")
@RequestMapping("/v1/safety")
public interface BlockedUserApi {

    @Operation(summary = "Get blocked users", description = "Get list of users blocked by current user")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Blocked users retrieved")})
    @GetMapping("/blocked")
    ResponseEntity<BlockedUsersResponse> getBlockedUsers();

    @Operation(summary = "Block a user", description = "Block another user")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User blocked successfully")})
    @PostMapping("/block")
    ResponseEntity<Map<String, Object>> blockUser(@RequestBody BlockUserRequest request);

    @Operation(summary = "Unblock a user", description = "Unblock a previously blocked user")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User unblocked successfully")})
    @PostMapping("/unblock")
    ResponseEntity<Map<String, Object>> unblockUser(@RequestBody UnblockUserRequest request);

    @Operation(summary = "Check if user is blocked", description = "Check if a specific user is blocked")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Block status retrieved")})
    @GetMapping("/blocked/{userId}")
    ResponseEntity<Map<String, Object>> isUserBlocked(@PathVariable long userId);
}

