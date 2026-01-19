package com.tpg.connect.profile.controller.api;

import com.tpg.connect.profile.model.request.UpdateProfileRequest;
import com.tpg.connect.profile.model.response.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.*;

@Tag(name = "Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public interface ProfileApi {

    @Operation(summary = "Get current user profile", description = "Retrieve the authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping(PROFILE_GET_ENDPOINT)
    ResponseEntity<ProfileResponse> getProfile();

    @Operation(summary = "Update current user profile", description = "Update the authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping(PROFILE_UPDATE_ENDPOINT)
    ResponseEntity<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request);

    @Operation(summary = "Add a photo to profile", description = "Upload a new photo for the current user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping(value = PROFILE_PHOTO_ADD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Map<String, String>> addPhoto(@RequestParam("photo") MultipartFile photo);

    @Operation(summary = "Remove a photo from profile", description = "Delete a photo from the current user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo removed successfully"),
            @ApiResponse(responseCode = "404", description = "Photo not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping(PROFILE_PHOTO_REMOVE)
    ResponseEntity<Void> removePhoto(@RequestParam("photoUrl") String photoUrl);
}
