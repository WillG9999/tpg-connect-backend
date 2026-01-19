package com.tpg.connect.integration.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationSubmissionApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    private String email;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String location;
    private List<String> bestQualities;
    private String reasonForJoining;
    private int responseStatus;
    private Map<String, Object> responseBody;

    @Given("a valid application request with email {string}")
    public void aValidApplicationRequestWithEmail(String email) {
        this.email = email;
    }

    @Given("an application request with email {string}")
    public void anApplicationRequestWithEmail(String email) {
        this.email = email;
    }

    @And("the application has firstName {string} and lastName {string}")
    public void theApplicationHasNames(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @And("the application has dateOfBirth {string} and gender {string}")
    public void theApplicationHasDobAndGender(String dateOfBirth, String gender) {
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    @And("the application has location {string}")
    public void theApplicationHasLocation(String location) {
        this.location = location;
    }

    @And("the application has bestQualities {string} and reasonForJoining {string}")
    public void theApplicationHasQualities(String bestQualities, String reasonForJoining) {
        this.bestQualities = Arrays.asList(bestQualities.split(","));
        this.reasonForJoining = reasonForJoining;
    }

    @When("the user submits the application")
    public void theUserSubmitsTheApplication() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("email", email);
        body.add("password", Base64.getEncoder().encodeToString("password123".getBytes()));
        body.add("firstName", firstName);
        body.add("lastName", lastName);
        body.add("dateOfBirth", dateOfBirth);
        body.add("gender", gender);
        body.add("location", location);
        if (bestQualities != null) {
            bestQualities.forEach(q -> body.add("bestQualities", q));
        }
        body.add("reasonForJoining", reasonForJoining);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/application/submit",
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

    @Then("the application response status should be {int}")
    public void theApplicationResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @And("the response should contain an applicationId starting with {string}")
    public void theResponseShouldContainApplicationId(String prefix) {
        assertNotNull(responseBody);
        String applicationId = (String) responseBody.get("applicationId");
        assertNotNull(applicationId);
        assertTrue(applicationId.startsWith(prefix));
    }
}

