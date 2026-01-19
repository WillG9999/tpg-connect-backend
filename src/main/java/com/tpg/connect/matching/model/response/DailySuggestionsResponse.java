package com.tpg.connect.matching.model.response;

import com.tpg.connect.matching.model.entity.DailySuggestion;

import java.util.List;

public record DailySuggestionsResponse(
        boolean success,
        String date,
        List<DailySuggestion> suggestions,
        int totalSuggestions,
        int remaining
) {
}

