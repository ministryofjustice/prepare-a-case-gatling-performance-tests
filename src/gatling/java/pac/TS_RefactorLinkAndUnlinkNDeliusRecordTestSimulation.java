package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Requirement 9: PACFS UI - Link / Unlink NDelius Record
 * Covers manual linking, unlinking, and the "No Match" flow for NDelius records.
 */
public class TS_RefactorLinkAndUnlinkNDeliusRecordTestSimulation extends Simulation implements PacSimulation {

    private static final FeederBuilder<String> courtFeeder = csv("data/courts.csv" ).random();
    private static final FeederBuilder<String> caseIdFeeder = csv("data/caseIds.csv").random(); // Need data/caseIds.csv
    private static final FeederBuilder<String> hearingIdFeeder = csv("data/hearingIds.csv").random(); // Need data/hearingIds.csv
    private static final FeederBuilder<String> defendantIdFeeder = csv("data/defendantIds.csv").random(); // Need data/defendantIds.csv

    // Feeder for valid CRNs (matching pattern ^[A-Za-z][0-9]{6}$)
    private static final List<Map<String, Object>> validCrnData = Arrays.asList(
            Collections.singletonMap("crn", "A123456"),
            Collections.singletonMap("crn", "B789012")
    );
    private static final FeederBuilder<Object> validCrnFeeder = listFeeder(validCrnData).random();

    // Feeder for invalid CRNs (for validation testing)
    private static final List<Map<String, Object>> invalidCrnData = Arrays.asList(
            Collections.singletonMap("crn", "1234567"), // Starts with number
            Collections.singletonMap("crn", "ABCDEFG")  // Wrong pattern
    );
    private static final FeederBuilder<Object> invalidCrnFeeder = listFeeder(invalidCrnData).random();

    // --- Feature 1: Link NDelius Record (Manual Match) ---

    // Access the manual link search page
    private static final ChainBuilder accessManualLinkPage = exec(
            http("Access Manual Link Page" )
                    .get("/#{CourtCode}/case/#{caseId}/hearing/#{hearingId}/defendant/#{defendantId}/manual")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Link NDelius Record")) // Assuming page title
    );

    // Search for a valid CRN
    private static final ChainBuilder searchValidCrn = exec(
            http("Search Valid CRN" )
                    .post("/#{CourtCode}/case/#{caseId}/hearing/#{hearingId}/defendant/#{defendantId}/manual")
                    .header("Authorization", "Bearer #{accessToken}")
                    .formParam("crn", "#{crn}") // Use CRN from feeder
                    .check(status().is(200)) // Assuming it redirects to confirmation page with 200
                    .check(css("h1").is("Confirm NDelius Record")) // Assuming confirmation page title
    );

    // Confirm the link
    private static final ChainBuilder confirmLink = exec(
            http("Confirm Link" )
                    .post("/#{CourtCode}/case/#{caseId}/hearing/#{hearingId}/defendant/#{defendantId}/manual/confirm") // Assuming a confirm endpoint
                    .header("Authorization", "Bearer #{accessToken}")
                    .formParam("confirm", "true") // Simulate confirmation
                    .check(status().is(303)) // Assuming a redirect after successful link
                    .check(header("Location").is("/#{CourtCode}/case/#{caseId}/hearing/#{hearingId}/defendant/#{defendantId}/summary")) // Redirect to summary page
    );

    // --- Feature 2: Unlink NDelius Record ---

    // Access the unlink page
    private static final ChainBuilder accessUnlinkPage = exec(
            http("Access Unlink Page" )
                    .get("/#{CourtCode}/case/#{caseId}/hearing/#{hearingId}/match/defendant/#{defendantId}/unlink/#{crn}") // Requires a CRN to be present
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Unlink NDelius Record")) // Assuming page title
    );

    // Submit the unlink form
    private static final ChainBuilder submitUnlinkForm = exec(
            http("Submit Unlink Form" )
                    .post("/#{CourtCode}/case/#{caseId}/hearing/#{hearingId}/match/defendant/#{defendantId}/unlink/#{crn}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .formParam("confirmUnlink", "true") // Simulate confirmation
                    .check(status().is(303)) // Assuming a redirect after successful unlink
                    .check(header("Location").is("/#{CourtCode}/case/#{caseId}/hearing/#{hearingId}/defendant/#{defendantId}/summary")) // Redirect to summary page
    );

    // --- Feature 3: "No Match" Flow ---

    // Select "No match" (simulates the action that triggers unlinkOffender API)
    private static final ChainBuilder selectNoMatch = exec(
            http("Select No Match" )
                    .get("/#{CourtCode}/case/#{caseId}/hearing/#{hearingId}/match/defendant/#{defendantId}/nomatch") // Assuming this GET triggers the action
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(303)) // Assuming a redirect after processing
                    .check(header("Location").is("/#{CourtCode}/case/#{caseId}/hearing/#{hearingId}/defendant/#{defendantId}/summary")) // Redirect to summary page
    );

    // Scenario for linking an NDelius record
    private static final ScenarioBuilder linkScenario = scenario("PACFS UI - Link NDelius Record")
            .exec(authenticate)
            .pause(pause)
            .feed(courtFeeder)
            .feed(caseIdFeeder)
            .feed(hearingIdFeeder)
            .feed(defendantIdFeeder)
            .feed(validCrnFeeder)
            .exec(accessManualLinkPage)
            .pause(pause)
            .exec(searchValidCrn)
            .pause(pause)
            .exec(confirmLink);

    // Scenario for unlinking an NDelius record
    private static final ScenarioBuilder unlinkScenario = scenario("PACFS UI - Unlink NDelius Record")
            .exec(authenticate)
            .pause(pause)
            .feed(courtFeeder)
            .feed(caseIdFeeder)
            .feed(hearingIdFeeder)
            .feed(defendantIdFeeder)
            .feed(validCrnFeeder) // Need a CRN to unlink
            .exec(accessUnlinkPage)
            .pause(pause)
            .exec(submitUnlinkForm);

    // Scenario for the "No Match" flow
    private static final ScenarioBuilder noMatchScenario = scenario("PACFS UI - No Match Flow")
            .exec(authenticate)
            .pause(pause)
            .feed(courtFeeder)
            .feed(caseIdFeeder)
            .feed(hearingIdFeeder)
            .feed(defendantIdFeeder)
            .exec(selectNoMatch);

    {
        setUp(
                linkScenario.injectOpen(rampUsers(users).during(ramp)),
                unlinkScenario.injectOpen(rampUsers(users).during(ramp)),
                noMatchScenario.injectOpen(rampUsers(users).during(ramp))
        ).protocols(httpProtocol );
    }
}
