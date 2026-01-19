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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;

public class ProfileApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Firestore firestore;

    @Autowired
    private JsonWebTokenProviderService jwtService;

    private int responseStatus;
    private Map<String, Object> responseBody;
    private String authToken;

    @Given("I am an authenticated user with connectId {string}")
    public void iAmAnAuthenticatedUserWithConnectId(String connectId) {
        long currentConnectId = Long.parseLong(connectId);
        authToken = jwtService.generateToken(currentConnectId, "test@example.com");
    }

    @And("a profile exists for connectId {string}")
    public void aProfileExistsForConnectId(String connectId) throws Exception {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("connectId", Long.parseLong(connectId));
        profileData.put("email", "test@example.com");
        profileData.put("firstName", "John");
        profileData.put("lastName", "Doe");
        profileData.put("dateOfBirth", "1990-01-15");
        profileData.put("gender", "Male");
        profileData.put("location", "San Francisco, CA");
        profileData.put("photoUrls", List.of());

        firestore.collection("Profiles")
                .document(connectId)
                .set(profileData)
                .get();
    }

    @And("no profile exists for connectId {string}")
    public void noProfileExistsForConnectId(String connectId) throws Exception {
        firestore.collection("Profiles")
                .document(connectId)
                .delete()
                .get();
    }

    @And("a profile exists for connectId {string} with photo {string}")
    public void aProfileExistsForConnectIdWithPhoto(String connectId, String photoUrl) throws Exception {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("connectId", Long.parseLong(connectId));
        profileData.put("email", "test@example.com");
        profileData.put("firstName", "John");
        profileData.put("lastName", "Doe");
        profileData.put("photoUrls", List.of(photoUrl));

        firestore.collection("Profiles")
                .document(connectId)
                .set(profileData)
                .get();
    }

    @When("I send a profile GET request to {string}")
    public void iSendAProfileGetRequestTo(String endpoint) {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + endpoint,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @When("I send a profile PUT request to {string} with body:")
    public void iSendAProfilePutRequestToWithBody(String endpoint, String body) {
        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    BASE_URL + endpoint,
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );
            responseStatus = response.getStatusCode().value();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @When("I send a multipart POST request to {string} with a photo")
    public void iSendAMultipartPostRequestToWithAPhoto(String endpoint) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(X_AUTHORISATION, "Bearer " + authToken);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        byte[] testImageBytes = new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01};

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-photo", ".jpg");
        java.nio.file.Files.write(tempFile, testImageBytes);

        org.springframework.core.io.FileSystemResource fileResource = new org.springframework.core.io.FileSystemResource(tempFile.toFile());

        org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("photo", fileResource);

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

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
            System.err.println("Photo upload failed with status: " + e.getStatusCode());
            System.err.println("Response body: " + e.getResponseBodyAsString());
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }

    @When("I send a profile DELETE request to {string} with photoUrl {string}")
    public void iSendAProfileDeleteRequestToWithPhotoUrl(String endpoint, String photoUrl) {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    BASE_URL + endpoint + "?photoUrl=" + photoUrl,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
            responseStatus = response.getStatusCode().value();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @Then("the profile response should be {int}")
    public void theProfileResponseShouldBe(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @And("the response should contain profile data")
    public void theResponseShouldContainProfileData() {
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("name"));
        assertNotNull(responseBody.get("location"));
    }

    @And("the response should contain a photo URL")
    public void theResponseShouldContainAPhotoUrl() {
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("photoUrl"));
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(X_AUTHORISATION, "Bearer " + authToken);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());
        return headers;
    }
}
