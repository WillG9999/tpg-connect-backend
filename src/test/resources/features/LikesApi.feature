Feature: Likes API
  As a user
  I want to see who has liked me and respond to their likes
  So that I can find mutual matches

  Scenario: Get received likes successfully
    Given I am an authenticated likes user with connectId "12345"
    And user "67890" has liked me
    When I request my received likes
    Then the likes response should be 200
    And the response should contain likes

  Scenario: Get received likes when none exist
    Given I am an authenticated likes user with connectId "99998"
    When I request my received likes
    Then the likes response should be 200
    And the response should contain no likes

  Scenario: Like back creates a match
    Given I am an authenticated likes user with connectId "12345"
    And user "67891" has liked me
    When I like back user "67891"
    Then the likes response should be 200
    And the response should indicate a new match

  Scenario: Pass on a like
    Given I am an authenticated likes user with connectId "12345"
    And user "67892" has liked me
    When I pass on user "67892"
    Then the likes response should be 200
    And the likes action should indicate success

