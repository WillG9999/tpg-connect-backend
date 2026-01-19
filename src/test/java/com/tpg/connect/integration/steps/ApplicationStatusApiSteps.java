package com.tpg.connect.integration.steps;

import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.repository.ApplicationRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationStatusApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ApplicationRepository applicationRepository;

    private int responseStatus;
    private Map<String, Object> responseBody;

    @Given("an application exists for email {string}")
    public void anApplicationExistsForEmail(String email) {
        Application application = Application.builder()
                .applicationId("APP-" + System.currentTimeMillis())
                .email(email.toLowerCase().trim())
                .hashedPassword("hashedPassword123")
                .firstName("Test")
                .lastName("User")
                .dateOfBirth("1990-01-01")
                .gender("Male")
                .location("Test City")
                .bestQualities(List.of("Honest", "Kind"))
                .reasonForJoining("Testing")
                .photoUrls(List.of())
                .status("pending")
                .createdAt(Instant.now())
                .build();
        applicationRepository.saveApplication(application);
    }

    @When("the user checks application status for email {string}")
    public void theUserChecksApplicationStatus(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        Map<String, String> request = Map.of("email", email);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/application/status",
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

    @Then("the status response should be {int}")
    public void theStatusResponseShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the status should be {string}")
    public void theStatusShouldBe(String expectedStatus) {
        assertNotNull(responseBody);
        assertEquals(expectedStatus, responseBody.get("status"));
    }
}

