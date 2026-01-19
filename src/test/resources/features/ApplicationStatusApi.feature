Feature: Application Status API
  As a user
  I want to check my application status
  So that I know if I have been approved

  Scenario: Check status of existing application
    Given an application exists for email "status-test@example.com"
    When the user checks application status for email "status-test@example.com"
    Then the status response should be 200
    And the status should be "pending"

  Scenario: Check status of non-existent application
    When the user checks application status for email "nonexistent@example.com"
    Then the status response should be 404

