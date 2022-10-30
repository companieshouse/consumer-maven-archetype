package ${package};

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public class Feature {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions resultActions;

    @Given("the service is healthy")
    public void serviceIsHealthy() {
        // noop; used in feature file as syntactic sugar
    }

    @When("the user performs a healthcheck")
    public void performLivenessCheck() throws Exception {
        resultActions = mockMvc.perform(get("/${artifactId}/healthcheck"));
    }

    @Then("the response code should be {int}")
    public void verifyResponseCode(int expected) throws Exception {
        resultActions.andExpect(status().is(expected));
    }
}
