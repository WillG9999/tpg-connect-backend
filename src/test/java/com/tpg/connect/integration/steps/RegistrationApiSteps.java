package com.tpg.connect.integration.steps;

import com.tpg.connect.user_registration.model.entity.request.UserRegistrationRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;

public class RegistrationApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";

    private final RestTemplate restTemplate = new RestTemplate();

    private String email;
    private String firstName = "Test";
    private String lastName = "User";
    private ResponseEntity<?> response;
    private int responseStatus;

    @Given("a valid registration request with email {string}")
    public void aValidRegistrationRequestWithEmail(String email) {
        this.email = email;
    }

    @Given("a registration request with missing email")
    public void aRegistrationRequestWithMissingEmail() {
        this.email = null;
    }

    @And("the request has firstName {string} and lastName {string}")
    public void theRequestHasFirstNameAndLastName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @When("the user submits the registration")
    public void theUserSubmitsTheRegistration() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                email,
                "password123",
                firstName,
                lastName,
                "1990-01-01",
                "male",
                "New York"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserRegistrationRequest> entity = new HttpEntity<>(request, headers);

        try {
            response = restTemplate.postForEntity(
                    BASE_URL + "/v1/auth/register",
                    entity,
                    Void.class
            );
            responseStatus = response.getStatusCode().value();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
            response = null;
        }
    }

    @When("the user submits the registration twice")
    public void theUserSubmitsTheRegistrationTwice() {
        theUserSubmitsTheRegistration();
        theUserSubmitsTheRegistration();
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @And("the response contains a bearer token")
    public void theResponseContainsABearerToken() {
        assertNotNull(response);
        assertNull(response.getBody());
        String authHeader = response.getHeaders().getFirst(X_AUTHORISATION);
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith("Bearer "));
        assertFalse(authHeader.substring(7).isEmpty());
    }
}
