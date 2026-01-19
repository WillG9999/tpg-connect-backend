package com.tpg.connect.integration.steps;

import com.tpg.connect.password_reset.model.entity.PasswordResetToken;
import com.tpg.connect.password_reset.repository.PasswordResetTokenRepository;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.RegisterUserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordResetApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private RegisterUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String resetToken;
    private int responseStatus;
    private Map<String, Object> responseBody;

    @Given("a registered user exists with email {string}")
    public void aRegisteredUserExistsWithEmail(String email) {
        RegisteredUser user = RegisteredUser.builder()
                .connectId(System.currentTimeMillis())
                .email(email.toLowerCase())
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .dateOfBirth("1990-01-01")
                .gender("Male")
                .location("Test City")
                .createdAt(Instant.now())
                .build();
        userRepository.saveUser(user);
    }

    @Given("a password reset token exists for email {string}")
    public void aPasswordResetTokenExists(String email) {
        // First create the user
        aRegisteredUserExistsWithEmail(email);

        // Then create the token
        this.resetToken = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .email(email.toLowerCase())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .used(false)
                .build();
        tokenRepository.save(token);
    }

    @When("the user requests password reset for email {string}")
    public void theUserRequestsPasswordReset(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        Map<String, String> request = Map.of("email", email);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/auth/forgot-password",
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

    @When("the user verifies the reset token")
    public void theUserVerifiesTheResetToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        Map<String, String> request = Map.of("token", resetToken);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/auth/verify-reset-token",
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

    @When("the user resets password with the token and new password {string}")
    public void theUserResetsPassword(String newPassword) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        Map<String, String> request = Map.of(
                "token", resetToken,
                "newPassword", newPassword
        );
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/auth/reset-password",
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

    @Then("the forgot password response status should be {int}")
    public void theForgotPasswordResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the verify token response status should be {int}")
    public void theVerifyTokenResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the reset password response status should be {int}")
    public void theResetPasswordResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @And("the response should indicate success")
    public void theResponseShouldIndicateSuccess() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
    }

    @And("the token should be valid")
    public void theTokenShouldBeValid() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("valid"));
    }

    @And("the password should be updated successfully")
    public void thePasswordShouldBeUpdatedSuccessfully() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
    }
}

