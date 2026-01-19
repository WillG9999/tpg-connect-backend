package com.tpg.connect.integration.steps;

import com.tpg.connect.email_verification.repository.VerificationCodeRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EmailVerificationApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    private int responseStatus;
    private Map<String, Object> responseBody;

    @Given("a verification code {string} exists for email {string}")
    public void aVerificationCodeExistsForEmail(String code, String email) {
        verificationCodeRepository.saveCode(email.toLowerCase(), code, 1800L);
    }

    @When("the user requests a verification code for email {string} with name {string}")
    public void theUserRequestsVerificationCode(String email, String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        Map<String, String> request = Map.of(
                "email", email,
                "userName", name
        );
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/auth/send-verification-code",
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

    @When("the user verifies the code {string} for email {string}")
    public void theUserVerifiesTheCode(String code, String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        Map<String, String> request = Map.of(
                "email", email,
                "verificationCode", code
        );
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/auth/verify-email-code",
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

    @Then("the verification code response status should be {int}")
    public void theVerificationCodeResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the verify code response status should be {int}")
    public void theVerifyCodeResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }
}

