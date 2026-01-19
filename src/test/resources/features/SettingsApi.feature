Feature: Settings API
  As a user
  I want to manage my notification, email, and privacy settings
  So that I can control my app experience

  Scenario: Get notification settings when none exist
    Given I am an authenticated settings user with connectId "99996"
    When I request my notification settings
    Then the settings response should be 200
    And the response should contain default notification settings

  Scenario: Update notification settings
    Given I am an authenticated settings user with connectId "12345"
    When I update my notification settings with pushEnabled false
    Then the settings response should be 200
    And the notification settings should have pushEnabled false

  Scenario: Get email settings when none exist
    Given I am an authenticated settings user with connectId "99995"
    When I request my email settings
    Then the settings response should be 200
    And the response should contain default email settings

  Scenario: Update email settings
    Given I am an authenticated settings user with connectId "12345"
    When I update my email settings with promotionalEmail true
    Then the settings response should be 200
    And the email settings should have promotionalEmail true

  Scenario: Get privacy settings when none exist
    Given I am an authenticated settings user with connectId "99994"
    When I request my privacy settings
    Then the settings response should be 200
    And the response should contain default privacy settings

  Scenario: Update privacy settings
    Given I am an authenticated settings user with connectId "12345"
    When I update my privacy settings with showOnlineStatus false
    Then the settings response should be 200
    And the privacy settings should have showOnlineStatus false

