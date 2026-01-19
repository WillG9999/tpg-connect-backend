package com.tpg.connect.matching.controller;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.matching.controller.api.LikesApi;
import com.tpg.connect.matching.model.response.LikeActionResponse;
import com.tpg.connect.matching.model.response.ReceivedLikesResponse;
import com.tpg.connect.matching.service.LikesService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LikesController implements LikesApi {

    private final LikesService likesService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<ReceivedLikesResponse> getReceivedLikes() {
        long connectId = extractConnectId();
        log.info("Getting received likes for connectId: {}", connectId);

        ReceivedLikesResponse response = likesService.getReceivedLikes(connectId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LikeActionResponse> likeBack(long userId) {
        long connectId = extractConnectId();
        log.info("User {} liking back user {}", connectId, userId);

        LikeActionResponse response = likesService.likeBack(connectId, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LikeActionResponse> passLike(long userId) {
        long connectId = extractConnectId();
        log.info("User {} passing on like from user {}", connectId, userId);

        LikeActionResponse response = likesService.passLike(connectId, userId);
        return ResponseEntity.ok(response);
    }

    private long extractConnectId() {
        String authHeader = httpServletRequest.getHeader(X_AUTHORISATION);
        String token = authHeader.replace("Bearer ", "");
        return jwtValidatorService.extractConnectId(token);
    }
}

