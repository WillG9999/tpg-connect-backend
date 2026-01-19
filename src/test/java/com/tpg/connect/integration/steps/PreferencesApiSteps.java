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
import java.util.Map;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;

public class PreferencesApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Firestore firestore;

    @Autowired
    private JsonWebTokenProviderService jwtService;

    private int responseStatus;
    private Map<String, Object> responseBody;
    private String authToken;

    @Given("I am an authenticated preferences user with connectId {string}")
    public void iAmAnAuthenticatedPreferencesUserWithConnectId(String connectId) {
        long currentConnectId = Long.parseLong(connectId);
        authToken = jwtService.generateToken(currentConnectId, "test@example.com");
    }

    @Given("I have saved preferences")
    public void iHaveSavedPreferences() throws Exception {
        Map<String, Object> prefsData = new HashMap<>();
        prefsData.put("connectId", 12345L);
        prefsData.put("preferredGender", "Women");
        prefsData.put("minAge", 25);
        prefsData.put("maxAge", 35);
        prefsData.put("maxDistance", 30);
        prefsData.put("distanceUnit", "miles");
        prefsData.put("showVerifiedOnly", true);

        firestore.collection("UserPreferences")
                .document("12345")
                .set(prefsData)
                .get();
    }

    @When("I request my preferences")
    public void iRequestMyPreferences() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/profile/preferences",
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

    @When("I update my preferences with minAge {int} and maxAge {int}")
    public void iUpdateMyPreferencesWithMinAgeAndMaxAge(int minAge, int maxAge) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            Map<String, Object> body = new HashMap<>();
            body.put("minAge", minAge);
            body.put("maxAge", maxAge);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/profile/preferences",
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @When("I reset my preferences")
    public void iResetMyPreferences() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/profile/preferences",
                    HttpMethod.DELETE,
                    entity,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @Then("the preferences response should be {int}")
    public void thePreferencesResponseShouldBe(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @And("the response should contain default preferences")
    public void theResponseShouldContainDefaultPreferences() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        Map<String, Object> prefs = (Map<String, Object>) responseBody.get("preferences");
        assertNotNull(prefs);
        assertEquals("Everyone", prefs.get("preferredGender"));
        assertEquals(18, prefs.get("minAge"));
        assertEquals(50, prefs.get("maxAge"));
    }

    @And("the response should contain updated preferences")
    public void theResponseShouldContainUpdatedPreferences() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        Map<String, Object> prefs = (Map<String, Object>) responseBody.get("preferences");
        assertNotNull(prefs);
        assertEquals(25, prefs.get("minAge"));
        assertEquals(35, prefs.get("maxAge"));
    }

    @And("the response should contain my saved preferences")
    public void theResponseShouldContainMySavedPreferences() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        Map<String, Object> prefs = (Map<String, Object>) responseBody.get("preferences");
        assertNotNull(prefs);
        assertEquals("Women", prefs.get("preferredGender"));
    }
}

