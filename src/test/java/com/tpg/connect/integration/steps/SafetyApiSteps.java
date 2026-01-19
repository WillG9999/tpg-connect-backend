package com.tpg.connect.integration.steps;

import com.tpg.connect.common.services.authentication.JsonWebTokenProviderService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SafetyApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private JsonWebTokenProviderService jwtService;

    private int responseStatus;
    private Map<String, Object> responseBody;
    private String authToken;

    @Given("I am a safety authenticated user with connectId {string}")
    public void iAmASafetyAuthenticatedUserWithConnectId(String connectId) {
        long currentConnectId = Long.parseLong(connectId);
        authToken = jwtService.generateToken(currentConnectId, "test@example.com");
    }

    @SuppressWarnings("unchecked")
    @When("I report user {string} for {string} with details {string}")
    public void iReportUserForWithDetails(String userId, String reason, String details) {
        String requestBody = String.format(
                "{\"reason\":\"%s\",\"details\":\"%s\",\"conversationId\":null}",
                reason, details
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/safety/users/" + userId + "/report",
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @SuppressWarnings("unchecked")
    @When("I block user {string}")
    public void iBlockUser(String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<String> request = new HttpEntity<>("{}", headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/safety/users/" + userId + "/block",
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @Given("user {string} is already blocked by me")
    public void userIsAlreadyBlockedByMe(String userId) {
        iBlockUser(userId);
    }

    @Given("user {string} is blocked by me")
    public void userIsBlockedByMe(String userId) {
        iBlockUser(userId);
    }

    @SuppressWarnings("unchecked")
    @When("I unblock user {string}")
    public void iUnblockUser(String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/safety/users/" + userId + "/block",
                    HttpMethod.DELETE,
                    request,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @Given("I have a conversation {string} with user {string}")
    public void iHaveAConversationWithUser(String conversationId, String userId) {
        // TODO: Create conversation in Firestore for testing
    }

    @SuppressWarnings("unchecked")
    @When("I unmatch from conversation {string}")
    public void iUnmatchFromConversation(String conversationId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<String> request = new HttpEntity<>("{}", headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/safety/conversations/" + conversationId + "/unmatch",
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @Then("the safety response should be {int}")
    public void theSafetyResponseShouldBe(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @Then("the safety response should indicate success")
    public void theSafetyResponseShouldIndicateSuccess() {
        assertTrue((Boolean) responseBody.get("success"));
    }

    @Then("the safety response should indicate success with message {string}")
    public void theSafetyResponseShouldIndicateSuccessWithMessage(String message) {
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals(message, responseBody.get("message"));
    }
}
