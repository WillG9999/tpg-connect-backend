package com.tpg.connect.settings.controller;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.settings.controller.api.AccountApi;
import com.tpg.connect.settings.model.request.DeactivateAccountRequest;
import com.tpg.connect.settings.model.response.AccountDataResponse;
import com.tpg.connect.settings.service.AccountService;
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
public class AccountController implements AccountApi {

    private final AccountService accountService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<Map<String, Object>> deactivateAccount(DeactivateAccountRequest request) {
        long connectId = extractConnectId();
        log.info("Deactivating account for: {}", connectId);
        boolean success = accountService.deactivateAccount(connectId, request.reason());
        return ResponseEntity.ok(Map.of("success", success, "message", success ? "Account deactivated" : "Failed to deactivate"));
    }

    @Override
    public ResponseEntity<AccountDataResponse> downloadAccountData() {
        long connectId = extractConnectId();
        log.info("Downloading account data for: {}", connectId);
        return ResponseEntity.ok(accountService.downloadAccountData(connectId));
    }

    @Override
    public ResponseEntity<Map<String, Object>> reactivateAccount() {
        long connectId = extractConnectId();
        log.info("Reactivating account for: {}", connectId);
        boolean success = accountService.reactivateAccount(connectId);
        return ResponseEntity.ok(Map.of("success", success, "message", success ? "Account reactivated" : "Failed to reactivate"));
    }

    private long extractConnectId() {
        String authHeader = httpServletRequest.getHeader(X_AUTHORISATION);
        String token = authHeader.replace("Bearer ", "");
        return jwtValidatorService.extractConnectId(token);
    }
}

