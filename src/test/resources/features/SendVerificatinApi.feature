Feature: Send Verification API

  Scenario: Successful verification code sending
    Given A User is in the process of registration with email
    When The user requests to send a verification code to the email
    Then The response status is 200
    And the User enters the received verification code