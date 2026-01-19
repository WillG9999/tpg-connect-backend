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

public class BlockedUsersApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Firestore firestore;

    @Autowired
    private JsonWebTokenProviderService jwtService;

    private int responseStatus;
    private Map<String, Object> responseBody;
    private String authToken;
    private long currentConnectId;

    @Given("I am an authenticated blocked user with connectId {string}")
    public void iAmAnAuthenticatedBlockedUserWithConnectId(String connectId) {
        currentConnectId = Long.parseLong(connectId);
        authToken = jwtService.generateToken(currentConnectId, "test@example.com");
    }

    @Given("I have blocked user with id {long}")
    public void iHaveBlockedUserWithId(long blockedUserId) throws Exception {
        Map<String, Object> blockData = new HashMap<>();
        blockData.put("blockId", "test-block-" + System.currentTimeMillis());
        blockData.put("blockerConnectId", currentConnectId);
        blockData.put("blockedConnectId", blockedUserId);
        blockData.put("reason", "Test block");
        blockData.put("blockedAt", Instant.now().toEpochMilli());

        firestore.collection("BlockedUsers")
                .document(blockData.get("blockId").toString())
                .set(blockData)
                .get();
    }

    @When("I request my blocked users list")
    public void iRequestMyBlockedUsersList() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/safety/blocked",
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

    @When("I block user with id {long} with reason {string}")
    public void iBlockUserWithIdWithReason(long userId, String reason) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            Map<String, Object> body = new HashMap<>();
            body.put("userIdToBlock", userId);
            body.put("reason", reason);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/safety/block",
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

    @When("I unblock user with id {long}")
    public void iUnblockUserWithId(long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            Map<String, Object> body = new HashMap<>();
            body.put("userIdToUnblock", userId);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/safety/unblock",
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

    @When("I check if user {long} is blocked")
    public void iCheckIfUserIsBlocked(long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/safety/blocked/" + userId,
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

    @Then("the blocked users response should be {int}")
    public void theBlockedUsersResponseShouldBe(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @And("the response should contain {int} blocked users")
    public void theResponseShouldContainBlockedUsers(int count) {
        assertNotNull(responseBody);
        List<?> blockedUsers = (List<?>) responseBody.get("blockedUsers");
        assertEquals(count, blockedUsers != null ? blockedUsers.size() : 0);
    }

    @And("the blocked response should indicate success")
    public void theBlockedResponseShouldIndicateSuccess() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
    }

    @And("the response should show user is blocked")
    public void theResponseShouldShowUserIsBlocked() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("isBlocked"));
    }
}

