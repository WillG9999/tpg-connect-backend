package com.tpg.connect.safety.controller;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.safety.controller.api.BlockedUserApi;
import com.tpg.connect.safety.model.request.BlockUserRequest;
import com.tpg.connect.safety.model.request.UnblockUserRequest;
import com.tpg.connect.safety.model.response.BlockedUsersResponse;
import com.tpg.connect.safety.service.BlockedUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BlockedUserController implements BlockedUserApi {

    private final BlockedUserService blockedUserService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<BlockedUsersResponse> getBlockedUsers() {
        long connectId = extractConnectId();
        log.info("Getting blocked users for connectId: {}", connectId);
        return ResponseEntity.ok(blockedUserService.getBlockedUsers(connectId));
    }

    @Override
    public ResponseEntity<Map<String, Object>> blockUser(BlockUserRequest request) {
        long connectId = extractConnectId();
        log.info("Blocking user: {} blocks {}", connectId, request.userIdToBlock());

        boolean success = blockedUserService.blockUser(connectId, request.userIdToBlock(), request.reason());
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "User blocked successfully" : "Failed to block user"
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> unblockUser(UnblockUserRequest request) {
        long connectId = extractConnectId();
        log.info("Unblocking user: {} unblocks {}", connectId, request.userIdToUnblock());

        boolean success = blockedUserService.unblockUser(connectId, request.userIdToUnblock());
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "User unblocked successfully" : "Failed to unblock user"
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> isUserBlocked(long userId) {
        long connectId = extractConnectId();
        boolean isBlocked = blockedUserService.isBlocked(connectId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "isBlocked", isBlocked
        ));
    }

    private long extractConnectId() {
        String authHeader = httpServletRequest.getHeader(X_AUTHORISATION);
        String token = authHeader.replace("Bearer ", "");
        return jwtValidatorService.extractConnectId(token);
    }
}

