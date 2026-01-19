package com.tpg.connect.settings.controller.api;

import com.tpg.connect.settings.model.request.DeactivateAccountRequest;
import com.tpg.connect.settings.model.response.AccountDataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Account", description = "Account management")
@RequestMapping("/v1/account")
public interface AccountApi {

    @Operation(summary = "Deactivate account", description = "Deactivate user account")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Account deactivated")})
    @PostMapping("/deactivate")
    ResponseEntity<Map<String, Object>> deactivateAccount(@RequestBody DeactivateAccountRequest request);

    @Operation(summary = "Download account data", description = "Download all user data (GDPR)")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Data retrieved")})
    @GetMapping("/download-data")
    ResponseEntity<AccountDataResponse> downloadAccountData();

    @Operation(summary = "Reactivate account", description = "Reactivate a deactivated account")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Account reactivated")})
    @PostMapping("/reactivate")
    ResponseEntity<Map<String, Object>> reactivateAccount();
}

