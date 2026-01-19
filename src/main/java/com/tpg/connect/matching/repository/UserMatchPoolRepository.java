package com.tpg.connect.matching.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.matching.mapper.MatchingMapper;
import com.tpg.connect.matching.model.entity.DailySuggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserMatchPoolRepository {

    private static final String COLLECTION_NAME = "UserMatchPools";
    private final Firestore firestore;
    private final MatchingMapper matchingMapper;

    @SuppressWarnings("unchecked")
    public Optional<List<DailySuggestion>> getTodaysSuggestions(long connectId) {
        return getSuggestionsForDate(connectId, LocalDate.now().toString());
    }

    @SuppressWarnings("unchecked")
    public Optional<List<DailySuggestion>> getSuggestionsForDate(long connectId, String date) {
        try {
            var doc = firestore.collection(COLLECTION_NAME)
                    .document(String.valueOf(connectId))
                    .get()
                    .get();

            if (!doc.exists()) {
                log.info("No match pool found for connectId: {}", connectId);
                return Optional.empty();
            }

            Map<String, Object> data = doc.getData();
            if (data == null || !data.containsKey("dailyEntries")) {
                return Optional.empty();
            }

            List<Map<String, Object>> dailyEntries = (List<Map<String, Object>>) data.get("dailyEntries");

            for (Map<String, Object> entry : dailyEntries) {
                String entryDate = (String) entry.get("date");
                if (date.equals(entryDate)) {
                    List<Map<String, Object>> matches = (List<Map<String, Object>>) entry.get("matches");
                    if (matches == null) {
                        return Optional.of(List.of());
                    }

                    List<DailySuggestion> suggestions = new ArrayList<>();
                    for (Map<String, Object> matchEntry : matches) {
                        suggestions.add(matchingMapper.mapPoolEntryToSuggestion(matchEntry));
                    }
                    return Optional.of(suggestions);
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get suggestions for connectId: {}", connectId, e);
            return Optional.empty();
        }
    }

    public boolean markAsViewed(long connectId, long suggestedConnectId) {
        try {
            log.info("Marking suggestion as viewed: {} -> {}", connectId, suggestedConnectId);
            return true;
        } catch (Exception e) {
            log.error("Failed to mark as viewed", e);
            return false;
        }
    }
}

