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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;

public class LikesApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Firestore firestore;

    @Autowired
    private JsonWebTokenProviderService jwtService;

    private int responseStatus;
    private Map<String, Object> responseBody;
    private String authToken;

    @Given("I am an authenticated likes user with connectId {string}")
    public void iAmAnAuthenticatedLikesUserWithConnectId(String connectId) {
        long currentConnectId = Long.parseLong(connectId);
        authToken = jwtService.generateToken(currentConnectId, "test@example.com");
    }

    @Given("user {string} has liked me")
    public void userHasLikedMe(String likerConnectId) throws Exception {
        String authHeader = "Bearer " + authToken;
        long likerId = Long.parseLong(likerConnectId);

        Map<String, Object> likeData = new HashMap<>();
        likeData.put("actorConnectId", likerId);
        likeData.put("targetConnectId", 12345L);
        likeData.put("action", "LIKE");
        likeData.put("date", java.time.LocalDate.now().toString());
        likeData.put("createdAt", Instant.now().toEpochMilli());
        likeData.put("processed", false);

        String actionId = "like_" + likerConnectId + "_" + System.currentTimeMillis();
        firestore.collection("MatchActions")
                .document(actionId)
                .set(likeData)
                .get();

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("connectId", likerId);
        profileData.put("firstName", "Liker" + likerConnectId);
        profileData.put("dateOfBirth", "1995-05-15");
        profileData.put("location", "New York");
        profileData.put("photoUrls", List.of("http://photo.jpg"));

        firestore.collection("Profiles")
                .document(likerConnectId)
                .set(profileData)
                .get();
    }

    @When("I request my received likes")
    public void iRequestMyReceivedLikes() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/likes/received",
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

    @When("I like back user {string}")
    public void iLikeBackUser(String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of(), headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/likes/" + userId + "/like-back",
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

    @When("I pass on user {string}")
    public void iPassOnUser(String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of(), headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/likes/" + userId + "/pass",
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

    @Then("the likes response should be {int}")
    public void theLikesResponseShouldBe(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @And("the response should contain likes")
    public void theResponseShouldContainLikes() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        List<?> likes = (List<?>) responseBody.get("likes");
        assertNotNull(likes);
        assertFalse(likes.isEmpty());
    }

    @And("the response should contain no likes")
    public void theResponseShouldContainNoLikes() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        List<?> likes = (List<?>) responseBody.get("likes");
        assertTrue(likes == null || likes.isEmpty());
    }

    @And("the response should indicate a new match")
    public void theResponseShouldIndicateANewMatch() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        assertNotNull(responseBody.get("newMatch"));
    }

    @And("the likes action should indicate success")
    public void theLikesActionResponseShouldIndicateSuccess() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
    }
}

