Feature: Profile API
  As an authenticated user
  I want to manage my profile
  So that I can update my information and photos

  Scenario: Get profile successfully
    Given I am an authenticated user with connectId "12345"
    And a profile exists for connectId "12345"
    When I send a profile GET request to "/v1/profile"
    Then the profile response should be 200
    And the response should contain profile data

  Scenario: Get profile returns 404 when not found
    Given I am an authenticated user with connectId "99999"
    And no profile exists for connectId "99999"
    When I send a profile GET request to "/v1/profile"
    Then the profile response should be 404

  Scenario: Update profile successfully
    Given I am an authenticated user with connectId "12345"
    And a profile exists for connectId "12345"
    When I send a profile PUT request to "/v1/profile" with body:
      """
      {
        "pronouns": "he/him",
        "jobTitle": "Senior Engineer",
        "bio": "Updated bio"
      }
      """
    Then the profile response should be 200

  Scenario: Add photo to profile successfully
    Given I am an authenticated user with connectId "12345"
    And a profile exists for connectId "12345"
    When I send a multipart POST request to "/v1/profile/photos" with a photo
    Then the profile response should be 200
    And the response should contain a photo URL

  Scenario: Remove photo returns 404 when photo not found
    Given I am an authenticated user with connectId "12345"
    And a profile exists for connectId "12345"
    When I send a profile DELETE request to "/v1/profile/photos" with photoUrl "http://storage/nonexistent.jpg"
    Then the profile response should be 404
