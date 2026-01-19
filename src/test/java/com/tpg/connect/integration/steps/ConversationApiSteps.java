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
import java.util.UUID;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;

public class ConversationApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Firestore firestore;

    @Autowired
    private JsonWebTokenProviderService jwtService;

    private int responseStatus;
    private Map<String, Object> responseBody;
    private List<Map<String, Object>> responseList;
    private String authToken;

    @Given("I am an authenticated conversation user with connectId {string}")
    public void iAmAnAuthenticatedConversationUserWithConnectId(String connectId) {
        long currentConnectId = Long.parseLong(connectId);
        authToken = jwtService.generateToken(currentConnectId, "test@example.com");
    }

    @And("a conversation exists between {string} and {string}")
    public void aConversationExistsBetween(String connectId1, String connectId2) throws Exception {
        long id1 = Long.parseLong(connectId1);
        long id2 = Long.parseLong(connectId2);
        String conversationId = Math.min(id1, id2) + "_" + Math.max(id1, id2);

        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("participants", List.of(id1, id2));
        conversationData.put("createdAt", Instant.now().toEpochMilli());

        firestore.collection("Conversations")
                .document(conversationId)
                .set(conversationData)
                .get();
    }

    @And("a conversation exists between {string} and {string} with messages")
    public void aConversationExistsBetweenWithMessages(String connectId1, String connectId2) throws Exception {
        aConversationExistsBetween(connectId1, connectId2);

        long id1 = Long.parseLong(connectId1);
        long id2 = Long.parseLong(connectId2);
        String conversationId = Math.min(id1, id2) + "_" + Math.max(id1, id2);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("conversationId", conversationId);
        messageData.put("senderId", id1);
        messageData.put("content", "Test message");
        messageData.put("timestamp", Instant.now().toEpochMilli());
        messageData.put("readAt", null);

        firestore.collection("Conversations")
                .document(conversationId)
                .collection("messages")
                .document(UUID.randomUUID().toString())
                .set(messageData)
                .get();
    }

    @And("a conversation exists between {string} and {string} with unread messages")
    public void aConversationExistsBetweenWithUnreadMessages(String connectId1, String connectId2) throws Exception {
        aConversationExistsBetweenWithMessages(connectId1, connectId2);
    }

    @When("I send a conversation GET request to {string}")
    public void iSendAConversationGetRequestTo(String endpoint) {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            if (endpoint.equals("/v1/conversations")) {
                ResponseEntity<List> response = restTemplate.exchange(
                        BASE_URL + endpoint,
                        HttpMethod.GET,
                        entity,
                        List.class
                );
                responseStatus = response.getStatusCode().value();
                responseList = response.getBody();
            } else {
                ResponseEntity<Map> response = restTemplate.exchange(
                        BASE_URL + endpoint,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );
                responseStatus = response.getStatusCode().value();
                responseBody = response.getBody();
            }
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @When("I send a conversation POST request to {string}")
    public void iSendAConversationPostRequestTo(String endpoint) {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + endpoint,
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

    @When("I send a message POST request to {string} with content {string}")
    public void iSendAMessagePostRequestToWithContent(String endpoint, String content) {
        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("content", content);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + endpoint,
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

    @Then("the conversation response should be {int}")
    public void theConversationResponseShouldBe(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @And("the response should contain conversation data")
    public void theResponseShouldContainConversationData() {
        assertNotNull(responseList != null ? responseList : responseBody);
    }

    @And("the response should contain paginated message data")
    public void theResponseShouldContainPaginatedMessageData() {
        assertNotNull(responseBody);
        assertTrue(responseBody.containsKey("messages") || responseBody.containsKey("hasMore"));
    }

    @And("the response should contain the sent message")
    public void theResponseShouldContainTheSentMessage() {
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("messageId"));
        assertNotNull(responseBody.get("content"));
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(X_AUTHORISATION, "Bearer " + authToken);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());
        return headers;
    }
}
