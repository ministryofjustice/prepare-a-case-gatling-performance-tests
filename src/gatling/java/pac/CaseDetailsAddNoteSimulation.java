package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

// 1. Rename the class to reflect its purpose
public class CaseDetailsAddNoteSimulation extends Simulation implements PacSimulation {

    // --- PREREQUISITE ACTIONS (from the baseline simulation ) ---

    // This action gets us to the case list and, crucially, extracts both a
    // case number and the CSRF token needed to submit the note form.
    private static ChainBuilder courtList = exec(
            http("Load Case List Page" )
                    .get("/select-court/" + courtCode)
                    .check(css("a.govuk-link[href*='/details']", "href")
                            .find()
                            .transform(href -> href.split("/")[3]) // Extracts the case number
                            .saveAs("caseNo")) // Saves it as 'caseNo'
                    .check(css("title").is("Case list - Prepare a case for sentence")));

    // This action gets us to the details page where the "add note" form is.
    // We also need to grab the CSRF token from this page to submit the form.
    private static ChainBuilder viewCaseDetails = exec(
            http("View Case Details Page" )
                    .get("/" + courtCode + "/case/#{caseNo}/details")
                    // Find the CSRF token on the page and save it
                    .check(css("input[name='_csrf']", "value").saveAs("csrf_token"))
                    .check(css("title").is("Case details - Prepare a case for sentence"))
    );


    // --- THE NEW ACTION FOR THIS SIMULATION ---

    // 2. Define the NEW "Add Note" action
    private static ChainBuilder addCaseNote = exec(
            http("Add a Note to the Case" )
                    .post("/" + courtCode + "/case/#{caseNo}/add-note") // The POST URL
                    .formParam("_csrf", "#{csrf_token}") // Use the CSRF token we just saved
                    .formParam("note", "This is a performance test note added by a Gatling simulation.") // The note content
                    // After adding a note, the app likely redirects back to the details page.
                    // We check for an element on that page to confirm success.
                    .check(status().is(200)) // Check for a successful HTTP status
                    .check(css("h1.govuk-heading-l").is("Case details")) // Confirm we are back on the details page
    );


    // --- THE SCENARIO ---

    // 3. Update the scenario to include the new action at the end
    ScenarioBuilder scenario = scenario("Add Note to Case") // A more specific scenario name
            .exec(homePage)
            .pause(pause)
            .exec(authenticate)
            .pause(pause)
            .exec(selectCourt)
            .pause(pause)
       //     .exec(saveCourt)
            .pause(pause)
            .exec(courtList)       // Step 1: Get to the case list
            .pause(pause)
            .exec(viewCaseDetails) // Step 2: View the specific case
            .pause(pause)
            .exec(addCaseNote);    // Step 3: Perform the "add note" action


    // --- THE SETUP (remains the same) ---
    {
        setUp(scenario.injectOpen(rampUsers(users).during(ramp))).protocols(httpProtocol );
    }
}

/**
 * Key Concepts in This New Script
 * Building on the Baseline: We didn't start from scratch. We reused the homePage, authenticate, selectCourt, saveCourt, courtList, and viewCaseDetails actions. This is the power of the framework's design.
 * Handling CSRF Tokens: Modern web applications use CSRF tokens to prevent malicious attacks. You cannot simply hardcode this value. The line .check(css("input[name='_csrf']", "value").saveAs("csrf_token")) finds the hidden token on the page and saves it, just like a real browser would. The .formParam("_csrf", "#{csrf_token}") then uses this saved value in the next request.
 * Simulating a POST Request: The .post(...) method is used to simulate submitting a form. The .formParam(...) calls add the data fields to the request body.
 * Chaining the Scenario: The final scenario is a complete, logical user journey: log in, find a case, view it, and then add a note.
 */