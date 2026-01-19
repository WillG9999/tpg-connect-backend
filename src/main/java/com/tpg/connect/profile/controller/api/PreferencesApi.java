package com.tpg.connect.profile.controller.api;

import com.tpg.connect.profile.model.request.UpdatePreferencesRequest;
import com.tpg.connect.profile.model.response.PreferencesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Preferences", description = "Discovery preferences management")
@RequestMapping("/v1/profile")
public interface PreferencesApi {

    @Operation(summary = "Get preferences", description = "Get user's discovery preferences")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully")
    })
    @GetMapping("/preferences")
    ResponseEntity<PreferencesResponse> getPreferences();

    @Operation(summary = "Update preferences", description = "Update user's discovery preferences")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences updated successfully")
    })
    @PutMapping("/preferences")
    ResponseEntity<PreferencesResponse> updatePreferences(@RequestBody UpdatePreferencesRequest request);

    @Operation(summary = "Reset preferences", description = "Reset preferences to defaults")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences reset successfully")
    })
    @DeleteMapping("/preferences")
    ResponseEntity<PreferencesResponse> resetPreferences();
}

