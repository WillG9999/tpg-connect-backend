Feature: Login API
  As a registered user
  I want to login
  So that I can access the application

  Scenario: Successfully login with valid credentials
    Given a registered user exists with email "loginuser@example.com" and password "password123"
    When the user logs in with email "loginuser@example.com" and password "password123"
    Then the login response status should be 200
    And the response should contain a JWT token

  Scenario: Login with invalid password
    Given a registered user exists with email "loginuser2@example.com" and password "correctpass"
    When the user logs in with email "loginuser2@example.com" and password "wrongpass"
    Then the login response status should be 401

  Scenario: Login with non-existent email
    When the user logs in with email "nobody@example.com" and password "anypass"
    Then the login response status should be 401

