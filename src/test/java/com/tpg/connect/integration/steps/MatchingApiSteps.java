package com.tpg.connect.integration.steps;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.common.services.authentication.JsonWebTokenProviderService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;

public class MatchingApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Firestore firestore;

    @Autowired
    private JsonWebTokenProviderService jwtService;

    private int responseStatus;
    private Map<String, Object> responseBody;
    private String authToken;

    @Given("I am an authenticated matching user with connectId {string}")
    public void iAmAnAuthenticatedMatchingUserWithConnectId(String connectId) {
        long currentConnectId = Long.parseLong(connectId);
        authToken = jwtService.generateToken(currentConnectId, "test@example.com");
    }

    @Given("match suggestions exist for user {string}")
    public void matchSuggestionsExistForUser(String connectId) throws Exception {
        String today = LocalDate.now().toString();

        Map<String, Object> matchEntry = new HashMap<>();
        matchEntry.put("matchConnectId", "67890");
        matchEntry.put("compatibilityScore", 0.85);
        matchEntry.put("stabilityRank", 1);
        matchEntry.put("viewed", false);

        Map<String, Object> dailyEntry = new HashMap<>();
        dailyEntry.put("date", today);
        dailyEntry.put("matches", List.of(matchEntry));
        dailyEntry.put("algorithmVersion", "gs-connect-v1.0");

        Map<String, Object> poolData = new HashMap<>();
        poolData.put("connectId", connectId);
        poolData.put("dailyEntries", List.of(dailyEntry));

        firestore.collection("UserMatchPools")
                .document(connectId)
                .set(poolData)
                .get();
    }

    @Given("no match suggestions exist for user {string}")
    public void noMatchSuggestionsExistForUser(String connectId) {
    }

    @Given("I have mutual matches")
    public void iHaveMutualMatches() throws Exception {
        Map<String, Object> matchData = new HashMap<>();
        matchData.put("connectId1", 12345L);
        matchData.put("connectId2", 67890L);
        matchData.put("compatibilityScore", 0.9);
        matchData.put("conversationId", "12345_67890");
        matchData.put("matchedAt", Instant.now().toEpochMilli());

        firestore.collection("MutualMatches")
                .document("12345_67890")
                .set(matchData)
                .get();

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("connectId", 67890L);
        profileData.put("firstName", "Jane");
        profileData.put("dateOfBirth", "1995-05-15");
        profileData.put("location", "New York");

        firestore.collection("Profiles")
                .document("67890")
                .set(profileData)
                .get();
    }

    @When("I request daily suggestions")
    public void iRequestDailySuggestions() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/discovery/suggestions",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @When("I submit a like action for user {string}")
    public void iSubmitALikeActionForUser(String userId) {
        submitAction(userId, "LIKE");
    }

    @When("I submit a pass action for user {string}")
    public void iSubmitAPassActionForUser(String userId) {
        submitAction(userId, "PASS");
    }

    private void submitAction(String userId, String action) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            Map<String, Object> actionItem = new HashMap<>();
            actionItem.put("targetConnectId", Long.parseLong(userId));
            actionItem.put("action", action);

            Map<String, Object> body = new HashMap<>();
            body.put("actions", List.of(actionItem));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/discovery/actions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @When("I request my matches")
    public void iRequestMyMatches() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/matches",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @Then("the matching response should be {int}")
    public void theMatchingResponseShouldBe(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @And("the response should contain suggestions")
    public void theResponseShouldContainSuggestions() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
    }

    @And("the response should contain empty suggestions")
    public void theResponseShouldContainEmptySuggestions() {
        assertNotNull(responseBody);
        Object success = responseBody.get("success");
        assertTrue(success == null || Boolean.TRUE.equals(success));
        List<?> suggestions = (List<?>) responseBody.get("suggestions");
        assertTrue(suggestions == null || suggestions.isEmpty());
    }

    @And("the action should be recorded")
    public void theActionShouldBeRecorded() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
    }

    @And("the response should contain match data")
    public void theResponseShouldContainMatchData() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
    }
}

