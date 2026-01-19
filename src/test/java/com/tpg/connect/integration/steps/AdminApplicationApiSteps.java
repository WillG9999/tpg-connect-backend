package com.tpg.connect.integration.steps;

import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.repository.ApplicationRepository;
import io.cucumber.java.en.And;
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

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;
import static org.junit.jupiter.api.Assertions.*;

public class AdminApplicationApiSteps {

    private static final String BASE_URL = "http://localhost:10000/api";
    private static final String TEST_TOKEN = "Token123";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ApplicationRepository applicationRepository;

    private int responseStatus;
    private Map<String, Object> responseBody;
    private String adminToken;

    @Given("there are multiple applications in the system")
    public void thereAreMultipleApplicationsInTheSystem() {
        for (int i = 0; i < 5; i++) {
            Application application = Application.builder()
                    .applicationId("APP-MULTI-" + System.currentTimeMillis() + "-" + i)
                    .email("multi" + System.currentTimeMillis() + i + "@example.com")
                    .hashedPassword("hashedPassword")
                    .firstName("Test" + i)
                    .lastName("User")
                    .dateOfBirth("1990-01-01")
                    .gender("Male")
                    .location("Test City")
                    .bestQualities(List.of("Honest", "Kind"))
                    .reasonForJoining("Testing")
                    .photoUrls(List.of())
                    .status(i % 2 == 0 ? "pending" : "approved")
                    .createdAt(Instant.now())
                    .build();
            applicationRepository.saveApplication(application);
        }
    }

    @Given("there are pending applications in the system")
    public void thereArePendingApplicationsInTheSystem() {
        Application application = Application.builder()
                .applicationId("APP-PENDING-" + System.currentTimeMillis())
                .email("pending" + System.currentTimeMillis() + "@example.com")
                .hashedPassword("hashedPassword")
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

    @Given("there are applications with various statuses")
    public void thereAreApplicationsWithVariousStatuses() {
        String[] statuses = {"pending", "approved", "rejected"};
        for (String status : statuses) {
            Application application = Application.builder()
                    .applicationId("APP-" + status.toUpperCase() + "-" + System.currentTimeMillis())
                    .email(status + System.currentTimeMillis() + "@example.com")
                    .hashedPassword("hashedPassword")
                    .firstName("Test")
                    .lastName("User")
                    .dateOfBirth("1990-01-01")
                    .gender("Male")
                    .location("Test City")
                    .bestQualities(List.of("Honest", "Kind"))
                    .reasonForJoining("Testing")
                    .photoUrls(List.of())
                    .status(status)
                    .createdAt(Instant.now())
                    .build();
            applicationRepository.saveApplication(application);
        }
    }

    @Given("an admin is authenticated")
    public void anAdminIsAuthenticated() {
        adminToken = TEST_TOKEN;
    }

    @Given("an application exists with id {string}")
    public void anApplicationExistsWithId(String applicationId) {
        Application application = Application.builder()
                .applicationId(applicationId)
                .email("app" + System.currentTimeMillis() + "@example.com")
                .hashedPassword("hashedPassword")
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

    @Given("an application exists with id {string} and status {string}")
    public void anApplicationExistsWithIdAndStatus(String applicationId, String status) {
        Application application = Application.builder()
                .applicationId(applicationId)
                .email("app" + System.currentTimeMillis() + "@example.com")
                .hashedPassword("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .dateOfBirth("1990-01-01")
                .gender("Male")
                .location("Test City")
                .bestQualities(List.of("Honest", "Kind"))
                .reasonForJoining("Testing")
                .photoUrls(List.of())
                .status(status)
                .createdAt(Instant.now())
                .build();
        applicationRepository.saveApplication(application);
    }

    @When("the admin requests all applications with page {int} and size {int}")
    public void theAdminRequestsAllApplicationsWithPageAndSize(int page, int size) {
        HttpHeaders headers = createAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/admin/applications?page=" + page + "&size=" + size,
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

    @When("the admin requests pending applications with page {int} and size {int}")
    public void theAdminRequestsPendingApplicationsWithPageAndSize(int page, int size) {
        HttpHeaders headers = createAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/admin/applications/pending?page=" + page + "&size=" + size,
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

    @When("the admin requests applications with status {string} page {int} and size {int}")
    public void theAdminRequestsApplicationsWithStatusPageAndSize(String status, int page, int size) {
        HttpHeaders headers = createAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/admin/applications/status/" + status + "?page=" + page + "&size=" + size,
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

    @When("the admin requests application {string}")
    public void theAdminRequestsApplication(String applicationId) {
        HttpHeaders headers = createAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/admin/applications/" + applicationId,
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

    @When("the admin approves application {string} with notes {string}")
    public void theAdminApprovesApplication(String applicationId, String notes) {
        HttpHeaders headers = createAuthenticatedHeaders();
        Map<String, String> request = Map.of("notes", notes);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/admin/applications/" + applicationId + "/approve",
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

    @When("the admin rejects application {string} with reason {string} and notes {string}")
    public void theAdminRejectsApplication(String applicationId, String reason, String notes) {
        HttpHeaders headers = createAuthenticatedHeaders();
        Map<String, String> request = Map.of(
                "rejectionReason", reason,
                "notes", notes
        );
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/admin/applications/" + applicationId + "/reject",
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

    @When("an unauthenticated user tries to approve application {string}")
    public void anUnauthenticatedUserTriesToApproveApplication(String applicationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        Map<String, String> request = Map.of("notes", "Test");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/admin/applications/" + applicationId + "/approve",
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @When("an unauthenticated user tries to get pending applications")
    public void anUnauthenticatedUserTriesToGetPendingApplications() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/v1/admin/applications/pending",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            responseStatus = response.getStatusCode().value();
        } catch (HttpStatusCodeException e) {
            responseStatus = e.getStatusCode().value();
        }
    }

    @Then("the all applications response status should be {int}")
    public void theAllApplicationsResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the pending applications response status should be {int}")
    public void thePendingApplicationsResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the by status response status should be {int}")
    public void theByStatusResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the get application response status should be {int}")
    public void theGetApplicationResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the approve application response status should be {int}")
    public void theApproveApplicationResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the reject application response status should be {int}")
    public void theRejectApplicationResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @And("the response should contain pagination info")
    public void theResponseShouldContainPaginationInfo() {
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("applications"));
        assertNotNull(responseBody.get("page"));
        assertNotNull(responseBody.get("size"));
        assertNotNull(responseBody.get("totalElements"));
        assertNotNull(responseBody.get("totalPages"));
    }

    @And("the response should contain pending applications")
    public void theResponseShouldContainPendingApplications() {
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("applications"));
    }

    @And("the response should contain application details")
    public void theResponseShouldContainApplicationDetails() {
        assertNotNull(responseBody);
        assertNotNull(responseBody.get("applicationId"));
        assertNotNull(responseBody.get("email"));
    }

    @And("the application status should be {string}")
    public void theApplicationStatusShouldBe(String expectedStatus) {
        assertNotNull(responseBody);
        assertEquals(expectedStatus, responseBody.get("status"));
    }

    private HttpHeaders createAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TPG-TXN-CORRELATION-ID", UUID.randomUUID().toString());
        headers.set(X_AUTHORISATION, "Bearer " + adminToken);
        return headers;
    }
}
