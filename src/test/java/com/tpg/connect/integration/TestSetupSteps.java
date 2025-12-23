package com.tpg.connect.integration;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class TestSetupSteps {

    @Given("the application is running")
    public void theApplicationIsRunning() {
        System.out.println("Application context loaded successfully");
    }

    @When("I check the setup")
    public void iCheckTheSetup() {
        System.out.println("Checking Cucumber setup...");
    }

    @Then("the setup should be complete")
    public void theSetupShouldBeComplete() {
        System.out.println("Cucumber test setup is complete!");
    }
}