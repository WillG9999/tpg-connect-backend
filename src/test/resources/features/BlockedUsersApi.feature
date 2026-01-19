Feature: Blocked Users API
  As a user
  I want to manage blocked users
  So that I can control who can interact with me

  Scenario: Get blocked users when none exist
    Given I am an authenticated blocked user with connectId "99990"
    When I request my blocked users list
    Then the blocked users response should be 200
    And the response should contain 0 blocked users

  Scenario: Block a user successfully
    Given I am an authenticated blocked user with connectId "12345"
    When I block user with id 67890 with reason "Spam"
    Then the blocked users response should be 200
    And the blocked response should indicate success

  Scenario: Unblock a user successfully
    Given I am an authenticated blocked user with connectId "12345"
    And I have blocked user with id 67890
    When I unblock user with id 67890
    Then the blocked users response should be 200
    And the blocked response should indicate success

  Scenario: Check if user is blocked
    Given I am an authenticated blocked user with connectId "12345"
    And I have blocked user with id 67890
    When I check if user 67890 is blocked
    Then the blocked users response should be 200
    And the response should show user is blocked

