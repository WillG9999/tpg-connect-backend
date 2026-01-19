Feature: Application Submission API
  As a user
  I want to submit an application
  So that I can join the platform

  Scenario: Successfully submit an application
    Given a valid application request with email "newapplicant@example.com"
    And the application has firstName "John" and lastName "Doe"
    And the application has dateOfBirth "1990-01-01" and gender "Male"
    And the application has location "San Francisco, CA"
    And the application has bestQualities "Honest,Kind" and reasonForJoining "Looking for connection"
    When the user submits the application
    Then the application response status should be 200
    And the response should contain an applicationId starting with "APP-"

  Scenario: Submit application with missing required fields
    Given an application request with email "incomplete@example.com"
    And the application has firstName "" and lastName ""
    When the user submits the application
    Then the application response status should be 400

