Feature: Password Reset API
  As a user
  I want to reset my password
  So that I can regain access to my account

  Scenario: Request password reset for existing user
    Given a registered user exists with email "resetuser@example.com"
    When the user requests password reset for email "resetuser@example.com"
    Then the forgot password response status should be 200
    And the response should indicate success

  Scenario: Request password reset for non-existent email
    When the user requests password reset for email "nobody@example.com"
    Then the forgot password response status should be 200
    And the response should indicate success

  Scenario: Verify valid reset token
    Given a password reset token exists for email "tokenuser@example.com"
    When the user verifies the reset token
    Then the verify token response status should be 200
    And the token should be valid

  Scenario: Reset password with valid token
    Given a password reset token exists for email "resetpass@example.com"
    When the user resets password with the token and new password "newPassword123"
    Then the reset password response status should be 200
    And the password should be updated successfully

