package com.tpg.connect.integration.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;

public class LogoutApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken;
    private int responseStatus;

    @Given("a user is logged in with a valid token")
    public void aUserIsLoggedInWithValidToken() {
        this.authToken = "Token123";
    }

    @When("the user logs out")
    public void theUserLogsOut() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());
        headers.set(X_AUTHORISATION, "Bearer " + authToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    BASE_URL + "/v1/auth/logout",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            responseStatus = response.getStatusCode().value();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @Then("the logout response status should be {int}")
    public void theLogoutResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }
}

