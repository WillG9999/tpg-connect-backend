Feature: Safety API
  As a user
  I want to report, block, and unmatch other users
  So that I can ensure a safe dating experience

  Background:
    Given I am a safety authenticated user with connectId "12345"

  Scenario: Report a user successfully
    When I report user "67890" for "HARASSMENT" with details "Sending inappropriate messages"
    Then the safety response should be 200
    And the safety response should indicate success

  Scenario: Block a user successfully
    When I block user "67890"
    Then the safety response should be 200
    And the safety response should indicate success with message "User blocked successfully"

  Scenario: Block an already blocked user
    Given user "67890" is already blocked by me
    When I block user "67890"
    Then the safety response should be 200
    And the safety response should indicate success with message "User already blocked"

  Scenario: Unblock a user successfully
    Given user "67890" is blocked by me
    When I unblock user "67890"
    Then the safety response should be 200
    And the safety response should indicate success with message "User unblocked successfully"

  Scenario: Unmatch from a conversation successfully
    Given I have a conversation "12345_67890" with user "67890"
    When I unmatch from conversation "12345_67890"
    Then the safety response should be 200
    And the safety response should indicate success with message "Unmatched successfully"
