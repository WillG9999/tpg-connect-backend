Feature: User Registration API

  Scenario: Successful user registration
    Given a valid registration request with email "test@example.com"
    And the request has firstName "John" and lastName "Doe"
    When the user submits the registration
    Then the response status is 200
    And the response contains a bearer token

  Scenario: Registration fails with missing email
    Given a registration request with missing email
    When the user submits the registration
    Then the response status is 400

  Scenario: Registration fails with duplicate email
    Given a valid registration request with email "test@example.com"
    And the request has firstName "John" and lastName "Doe"
    When the user submits the registration
    And the user submits the registration
    Then the response status is 500
