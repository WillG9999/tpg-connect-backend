package com.tpg.connect.matching.controller.api;

import com.tpg.connect.matching.model.request.SubmitActionsRequest;
import com.tpg.connect.matching.model.response.DailySuggestionsResponse;
import com.tpg.connect.matching.model.response.MatchesListResponse;
import com.tpg.connect.matching.model.response.SubmitActionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Matching", description = "Daily match suggestions and actions")
@RequestMapping("/v1")
public interface MatchingApi {

    @Operation(summary = "Get daily suggestions", description = "Get today's match suggestions from the daily batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggestions retrieved successfully")
    })
    @GetMapping("/discovery/suggestions")
    ResponseEntity<DailySuggestionsResponse> getDailySuggestions();

    @Operation(summary = "Submit match actions", description = "Submit batch of like/pass actions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actions processed successfully")
    })
    @PostMapping("/discovery/actions")
    ResponseEntity<SubmitActionsResponse> submitActions(@RequestBody SubmitActionsRequest request);

    @Operation(summary = "Get matches", description = "Get list of confirmed mutual matches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matches retrieved successfully")
    })
    @GetMapping("/matches")
    ResponseEntity<MatchesListResponse> getMatches();
}

