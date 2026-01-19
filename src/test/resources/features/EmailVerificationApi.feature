Feature: Email Verification API
  As a user
  I want to verify my email
  So that I can complete my registration

  Scenario: Successfully send verification code
    When the user requests a verification code for email "verify@example.com" with name "John Doe"
    Then the verification code response status should be 200

  Scenario: Successfully verify email code
    Given a verification code "123456" exists for email "verified@example.com"
    When the user verifies the code "123456" for email "verified@example.com"
    Then the verify code response status should be 200

  Scenario: Verify with invalid code
    Given a verification code "123456" exists for email "wrongcode@example.com"
    When the user verifies the code "999999" for email "wrongcode@example.com"
    Then the verify code response status should be 400

