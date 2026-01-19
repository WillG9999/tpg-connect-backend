package com.tpg.connect.matching.controller;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.matching.controller.api.MatchingApi;
import com.tpg.connect.matching.model.request.SubmitActionsRequest;
import com.tpg.connect.matching.model.response.DailySuggestionsResponse;
import com.tpg.connect.matching.model.response.MatchesListResponse;
import com.tpg.connect.matching.model.response.SubmitActionsResponse;
import com.tpg.connect.matching.service.MatchingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MatchingController implements MatchingApi {

    private final MatchingService matchingService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<DailySuggestionsResponse> getDailySuggestions() {
        long connectId = extractConnectId();
        log.info("Getting daily suggestions for connectId: {}", connectId);

        DailySuggestionsResponse response = matchingService.getDailySuggestions(connectId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SubmitActionsResponse> submitActions(SubmitActionsRequest request) {
        long connectId = extractConnectId();
        log.info("Submitting {} actions for connectId: {}", request.actions().size(), connectId);

        SubmitActionsResponse response = matchingService.submitActions(connectId, request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MatchesListResponse> getMatches() {
        long connectId = extractConnectId();
        log.info("Getting matches for connectId: {}", connectId);

        MatchesListResponse response = matchingService.getMatches(connectId);
        return ResponseEntity.ok(response);
    }

    private long extractConnectId() {
        String authHeader = httpServletRequest.getHeader(X_AUTHORISATION);
        String token = authHeader.replace("Bearer ", "");
        return jwtValidatorService.extractConnectId(token);
    }
}

