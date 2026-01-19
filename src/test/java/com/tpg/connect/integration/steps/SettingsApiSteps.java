package com.tpg.connect.integration.steps;

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

public class SettingsApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private JsonWebTokenProviderService jwtService;

    private int responseStatus;
    private Map<String, Object> responseBody;
    private String authToken;

    @Given("I am an authenticated settings user with connectId {string}")
    public void iAmAnAuthenticatedSettingsUserWithConnectId(String connectId) {
        long currentConnectId = Long.parseLong(connectId);
        authToken = jwtService.generateToken(currentConnectId, "test@example.com");
    }

    @When("I request my notification settings")
    public void iRequestMyNotificationSettings() {
        makeGetRequest("/v1/settings/notifications/push");
    }

    @When("I update my notification settings with pushEnabled false")
    public void iUpdateMyNotificationSettingsWithPushEnabledFalse() {
        Map<String, Object> body = new HashMap<>();
        body.put("pushEnabled", false);
        makePutRequest("/v1/settings/notifications/push", body);
    }

    @When("I request my email settings")
    public void iRequestMyEmailSettings() {
        makeGetRequest("/v1/settings/notifications/email");
    }

    @When("I update my email settings with promotionalEmail true")
    public void iUpdateMyEmailSettingsWithPromotionalEmailTrue() {
        Map<String, Object> body = new HashMap<>();
        body.put("promotionalEmail", true);
        makePutRequest("/v1/settings/notifications/email", body);
    }

    @When("I request my privacy settings")
    public void iRequestMyPrivacySettings() {
        makeGetRequest("/v1/settings/privacy");
    }

    @When("I update my privacy settings with showOnlineStatus false")
    public void iUpdateMyPrivacySettingsWithShowOnlineStatusFalse() {
        Map<String, Object> body = new HashMap<>();
        body.put("showOnlineStatus", false);
        makePutRequest("/v1/settings/privacy", body);
    }

    @Then("the settings response should be {int}")
    public void theSettingsResponseShouldBe(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @And("the response should contain default notification settings")
    public void theResponseShouldContainDefaultNotificationSettings() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        Map<String, Object> settings = (Map<String, Object>) responseBody.get("settings");
        assertNotNull(settings);
        assertTrue((Boolean) settings.get("pushEnabled"));
        assertTrue((Boolean) settings.get("newMatchNotification"));
    }

    @And("the notification settings should have pushEnabled false")
    public void theNotificationSettingsShouldHavePushEnabledFalse() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        Map<String, Object> settings = (Map<String, Object>) responseBody.get("settings");
        assertFalse((Boolean) settings.get("pushEnabled"));
    }

    @And("the response should contain default email settings")
    public void theResponseShouldContainDefaultEmailSettings() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        Map<String, Object> settings = (Map<String, Object>) responseBody.get("settings");
        assertNotNull(settings);
        assertTrue((Boolean) settings.get("emailEnabled"));
    }

    @And("the email settings should have promotionalEmail true")
    public void theEmailSettingsShouldHavePromotionalEmailTrue() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        Map<String, Object> settings = (Map<String, Object>) responseBody.get("settings");
        assertTrue((Boolean) settings.get("promotionalEmail"));
    }

    @And("the response should contain default privacy settings")
    public void theResponseShouldContainDefaultPrivacySettings() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        Map<String, Object> settings = (Map<String, Object>) responseBody.get("settings");
        assertNotNull(settings);
        assertTrue((Boolean) settings.get("showOnlineStatus"));
    }

    @And("the privacy settings should have showOnlineStatus false")
    public void thePrivacySettingsShouldHaveShowOnlineStatusFalse() {
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        Map<String, Object> settings = (Map<String, Object>) responseBody.get("settings");
        assertFalse((Boolean) settings.get("showOnlineStatus"));
    }

    private void makeGetRequest(String path) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + path,
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

    private void makePutRequest(String path, Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_AUTHORISATION, "Bearer " + authToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + path,
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
}

