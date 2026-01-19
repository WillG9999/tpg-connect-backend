Feature: Admin Application Management API
  As an admin
  I want to manage user applications
  So that I can approve or reject membership requests

  Scenario: Get all applications with pagination
    Given there are multiple applications in the system
    And an admin is authenticated
    When the admin requests all applications with page 0 and size 10
    Then the all applications response status should be 200
    And the response should contain pagination info

  Scenario: Get pending applications with pagination
    Given there are pending applications in the system
    And an admin is authenticated
    When the admin requests pending applications with page 0 and size 10
    Then the pending applications response status should be 200
    And the response should contain pending applications

  Scenario: Get applications by status
    Given there are applications with various statuses
    And an admin is authenticated
    When the admin requests applications with status "approved" page 0 and size 10
    Then the by status response status should be 200

  Scenario: Get application by ID
    Given an application exists with id "APP-TESTID123"
    And an admin is authenticated
    When the admin requests application "APP-TESTID123"
    Then the get application response status should be 200
    And the response should contain application details

  Scenario: Get application that does not exist
    Given an admin is authenticated
    When the admin requests application "APP-NOTEXIST"
    Then the get application response status should be 404

  Scenario: Approve application successfully
    Given an application exists with id "APP-APPROVE123" and status "pending"
    And an admin is authenticated
    When the admin approves application "APP-APPROVE123" with notes "Great profile"
    Then the approve application response status should be 200
    And the application status should be "approved"

  Scenario: Reject application successfully
    Given an application exists with id "APP-REJECT123" and status "pending"
    And an admin is authenticated
    When the admin rejects application "APP-REJECT123" with reason "incomplete_profile" and notes "Missing photos"
    Then the reject application response status should be 200
    And the application status should be "rejected"

  Scenario: Approve application without authentication
    When an unauthenticated user tries to approve application "APP-123"
    Then the response status should be 401

  Scenario: Get pending applications without authentication
    When an unauthenticated user tries to get pending applications
    Then the response status should be 401
