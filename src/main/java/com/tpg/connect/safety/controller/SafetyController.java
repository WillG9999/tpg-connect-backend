package com.tpg.connect.safety.controller;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.safety.controller.api.SafetyApi;
import com.tpg.connect.safety.model.request.ReportUserRequest;
import com.tpg.connect.safety.model.response.SafetyActionResponse;
import com.tpg.connect.safety.service.SafetyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SafetyController implements SafetyApi {

    private final SafetyService safetyService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<SafetyActionResponse> reportUser(long userId, ReportUserRequest request) {
        long connectId = extractConnectId();
        log.info("User {} reporting user {}", connectId, userId);

        SafetyActionResponse response = safetyService.reportUser(connectId, userId, request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SafetyActionResponse> blockUser(long userId) {
        long connectId = extractConnectId();
        log.info("User {} blocking user {}", connectId, userId);

        SafetyActionResponse response = safetyService.blockUser(connectId, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SafetyActionResponse> unblockUser(long userId) {
        long connectId = extractConnectId();
        log.info("User {} unblocking user {}", connectId, userId);

        SafetyActionResponse response = safetyService.unblockUser(connectId, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SafetyActionResponse> unmatch(String conversationId) {
        long connectId = extractConnectId();
        log.info("User {} unmatching conversation {}", connectId, conversationId);

        SafetyActionResponse response = safetyService.unmatch(connectId, conversationId);
        return ResponseEntity.ok(response);
    }

    private long extractConnectId() {
        String authHeader = httpServletRequest.getHeader(X_AUTHORISATION);
        String token = authHeader.replace("Bearer ", "");
        return jwtValidatorService.extractConnectId(token);
    }
}
