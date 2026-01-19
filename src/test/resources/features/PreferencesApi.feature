Feature: Preferences API
  As a user
  I want to manage my discovery preferences
  So that I can control who I see in my daily suggestions

  Scenario: Get preferences when none exist
    Given I am an authenticated preferences user with connectId "99997"
    When I request my preferences
    Then the preferences response should be 200
    And the response should contain default preferences

  Scenario: Update preferences successfully
    Given I am an authenticated preferences user with connectId "12345"
    When I update my preferences with minAge 25 and maxAge 35
    Then the preferences response should be 200
    And the response should contain updated preferences

  Scenario: Get existing preferences
    Given I am an authenticated preferences user with connectId "12345"
    And I have saved preferences
    When I request my preferences
    Then the preferences response should be 200
    And the response should contain my saved preferences

  Scenario: Reset preferences to defaults
    Given I am an authenticated preferences user with connectId "12345"
    And I have saved preferences
    When I reset my preferences
    Then the preferences response should be 200
    And the response should contain default preferences

