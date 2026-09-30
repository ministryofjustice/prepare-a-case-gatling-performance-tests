package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

// 1. Rename the class
public class CaseDetailsSimulation extends Simulation implements PacSimulation {

    // This is a prerequisite from PacSimulation, but we need it in the flow
    private static ChainBuilder courtList = exec(
            http("Load Case List Page" )
                    .get("/select-court/"+courtCode)
                    // We need to extract a case number to click on.
                    // This check finds a link with a URL like "/B14LO/case/123456/details"
                    // and saves the "123456" part into a variable called "caseNo".
                    .check(css("a.govuk-link[href*='/details']", "href")
                            .find()
                            .transform(href -> href.split("/")[3]) // Extracts the case number from the URL
                            .saveAs("caseNo")) // Saves it as 'caseNo'
                    .check(css("title").is("Case list - Prepare a case for sentence")));

    // 2. Define the NEW action for this simulation
    private static ChainBuilder viewCaseDetails = exec(
            http("View Case Details Page" )
                    // Use the 'caseNo' variable we just saved
                    .get("/" + courtCode + "/case/#{caseNo}/details")
                    .check(css("title").is("Case details - Prepare a case for sentence")) // Check the new page title
    );

    // 3. Update the scenario to include the new action
    ScenarioBuilder scenario = scenario("View Case Details") // Give it a descriptive name
            .exec(homePage)
            .pause(pause)
            .exec(authenticate)
            .pause(pause)
            .exec(selectCourt)
            .pause(pause)
    //        .exec(saveCourt)
            .pause(pause)
            .exec(courtList) // First, load the list
            .pause(pause)
            .exec(viewCaseDetails); // THEN, view the details

    // 4. The setUp block remains the same
    {
        setUp(scenario.injectOpen(rampUsers(users).during(ramp))).protocols(httpProtocol );
    }
}
/**
 * Key Changes Explained:
 * Extracting Data (.saveAs("caseNo")): A real user doesn't know the case number beforehand. They find it on the case list page. The line .check(css(...).saveAs("caseNo")) simulates this: it finds the first link to a details page, extracts the case number from its URL, and saves it into a session variable called caseNo.
 * Using Data (#{caseNo}): The viewCaseDetails action then uses this variable with #{caseNo} to build the correct URL, simulating a user clicking the link.
 * New Scenario Flow: The scenario now includes the viewCaseDetails step at the end.
 */