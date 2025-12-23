Feature: Test Setup Verification
  This feature verifies that the Cucumber test setup is working correctly
  
  Scenario: Basic setup verification
    Given the application is running
    When I check the setup
    Then the setup should be complete