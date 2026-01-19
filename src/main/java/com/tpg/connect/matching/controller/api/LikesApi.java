package com.tpg.connect.matching.controller.api;

import com.tpg.connect.matching.model.response.LikeActionResponse;
import com.tpg.connect.matching.model.response.ReceivedLikesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Likes", description = "Manage users who have liked you")
@RequestMapping("/v1/likes")
public interface LikesApi {

    @Operation(summary = "Get received likes", description = "Get list of users who have liked you")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Likes retrieved successfully")
    })
    @GetMapping("/received")
    ResponseEntity<ReceivedLikesResponse> getReceivedLikes();

    @Operation(summary = "Like back", description = "Like back a user who has liked you, creating a mutual match")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like back processed successfully")
    })
    @PostMapping("/{userId}/like-back")
    ResponseEntity<LikeActionResponse> likeBack(@PathVariable long userId);

    @Operation(summary = "Pass on like", description = "Pass on a user who has liked you")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pass processed successfully")
    })
    @PostMapping("/{userId}/pass")
    ResponseEntity<LikeActionResponse> passLike(@PathVariable long userId);
}

