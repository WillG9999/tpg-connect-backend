 package com.tpg.connect.integration.steps;

 import com.tpg.connect.email_verification.model.request.SendVerificationCodeRequest;
 import com.tpg.connect.email_verification.repository.VerificationCodeRepository;
 import io.cucumber.java.en.And;
 import io.cucumber.java.en.Given;
 import io.cucumber.java.en.Then;
 import io.cucumber.java.en.When;
 import io.jsonwebtoken.Jwts;
 import io.jsonwebtoken.io.Decoders;
 import io.jsonwebtoken.security.Keys;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.HttpStatusCodeException;
 import org.springframework.web.client.RestTemplate;

 import javax.crypto.SecretKey;
 import java.util.Date;

 import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.SEND_VERIFICATION_ENDPOINT;
 import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.VERIFY_EMAIL_CODE_ENDPOINT;
 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SendVerificationApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.access-token-expiration:3600000}")
    private long jwtExpiration;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    private String email;
    private String userName;
    private String authToken;
    private ResponseEntity<?> response;
    private int responseStatus;

    @Given("A User is in the process of registration with email")
    public void aUserIsInTheProcessOfRegistrationWithEmail() {
        this.email = "will_graham@live.com";
        this.userName = "will_graham";
        this.authToken = generateJwtToken(email);
    }

    @When("The user requests to send a verification code to the email")
    public void theUserRequestsToSendAVerificationCodeToTheEmail() {
        String url = BASE_URL + SEND_VERIFICATION_ENDPOINT;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authToken);

        SendVerificationCodeRequest requestBody = new SendVerificationCodeRequest(email, userName);
        HttpEntity<SendVerificationCodeRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            response = restTemplate.postForEntity(url, entity, Void.class);
            responseStatus = response.getStatusCode().value();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
            response = null;
        }
    }

    @Then("The response status is {int}")
    public void theResponseStatusIs(int statusCode) {
        assertEquals(statusCode, responseStatus,
                "Expected status " + statusCode + " but got " + responseStatus);
    }

    @And("the verification code is sent successfully to will_graham@live.com")
    public void theVerificationCodeIsSentSuccessfully() {
        assertNotNull(response, "Response should not be null");
        assertEquals(200, responseStatus, "Verification code should be sent successfully");
    }

    @And("the User enters the received verification code")
    public void theUserEntersTheReceivedVerificationCode() {
        String verificationCode = verificationCodeRepository.retrieveCode(email);
        assertNotNull(verificationCode, "Verification code should have been generated and saved");

        String url = BASE_URL + VERIFY_EMAIL_CODE_ENDPOINT + "?email=" + email + "&verificationCode=" + verificationCode;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            response = restTemplate.postForEntity(url, entity, Void.class);
            responseStatus = response.getStatusCode().value();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
            response = null;
        }
    }

    private String generateJwtToken(String email) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            return Jwts.builder()
                    .subject(email)
                    .claim("email", email)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
}
