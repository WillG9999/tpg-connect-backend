Feature: Matching API
  As a user
  I want to get daily match suggestions and submit actions
  So that I can find compatible matches

  Scenario: Get daily suggestions successfully
    Given I am an authenticated matching user with connectId "12345"
    And match suggestions exist for user "12345"
    When I request daily suggestions
    Then the matching response should be 200
    And the response should contain suggestions

  Scenario: Get daily suggestions when none exist
    Given I am an authenticated matching user with connectId "99999"
    And no match suggestions exist for user "99999"
    When I request daily suggestions
    Then the matching response should be 200
    And the response should contain empty suggestions

  Scenario: Submit like action successfully
    Given I am an authenticated matching user with connectId "12345"
    When I submit a like action for user "67890"
    Then the matching response should be 200
    And the action should be recorded

  Scenario: Submit pass action successfully
    Given I am an authenticated matching user with connectId "12345"
    When I submit a pass action for user "67890"
    Then the matching response should be 200
    And the action should be recorded

  Scenario: Get matches successfully
    Given I am an authenticated matching user with connectId "12345"
    And I have mutual matches
    When I request my matches
    Then the matching response should be 200
    And the response should contain match data

