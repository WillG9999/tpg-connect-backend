package com.tpg.connect.integration.steps;

import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.RegisterUserRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LoginApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RegisterUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private int responseStatus;
    private Map<String, Object> responseBody;

    @Given("a registered user exists with email {string} and password {string}")
    public void aRegisteredUserExists(String email, String password) {
        RegisteredUser user = RegisteredUser.builder()
                .connectId(System.currentTimeMillis())
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(password))
                .firstName("Test")
                .lastName("User")
                .dateOfBirth("1990-01-01")
                .gender("Male")
                .location("Test City")
                .createdAt(Instant.now())
                .build();
        userRepository.saveUser(user);
    }

    @When("the user logs in with email {string} and password {string}")
    public void theUserLogsIn(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        String base64Password = Base64.getEncoder().encodeToString(password.getBytes());
        Map<String, String> request = Map.of(
                "email", email,
                "password", base64Password
        );
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/auth/login",
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

    @Then("the login response status should be {int}")
    public void theLoginResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the response should contain a JWT token")
    public void theResponseShouldContainJwtToken() {
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("accessToken"));
        String token = (String) responseBody.get("accessToken");
        assertTrue(token.length() > 0);
        assertTrue(token.contains("."));
    }
}

