Feature: Logout API
  As a logged in user
  I want to logout
  So that my session is ended

  Scenario: Successfully logout
    Given a user is logged in with a valid token
    When the user logs out
    Then the logout response status should be 200

