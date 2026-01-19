Feature: Conversation API
  As an authenticated user
  I want to manage my conversations
  So that I can communicate with other users

  Scenario: Get user conversations
    Given I am an authenticated conversation user with connectId "12345"
    And a conversation exists between "12345" and "67890"
    When I send a conversation GET request to "/v1/conversations"
    Then the conversation response should be 200
    And the response should contain conversation data

  Scenario: Get conversation messages with pagination
    Given I am an authenticated conversation user with connectId "12345"
    And a conversation exists between "12345" and "67890" with messages
    When I send a conversation GET request to "/v1/conversations/12345_67890/messages?limit=50"
    Then the conversation response should be 200
    And the response should contain paginated message data

  Scenario: Send a message
    Given I am an authenticated conversation user with connectId "12345"
    And a conversation exists between "12345" and "67890"
    When I send a message POST request to "/v1/conversations/12345_67890/messages" with content "Hello!"
    Then the conversation response should be 200
    And the response should contain the sent message

  Scenario: Mark messages as read
    Given I am an authenticated conversation user with connectId "12345"
    And a conversation exists between "12345" and "67890" with unread messages
    When I send a conversation POST request to "/v1/conversations/12345_67890/read"
    Then the conversation response should be 200

  Scenario: Get or create conversation
    Given I am an authenticated conversation user with connectId "12345"
    When I send a conversation POST request to "/v1/conversations/with/67890"
    Then the conversation response should be 200
    And the response should contain conversation data
