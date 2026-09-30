package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Requirement 11: PACFS UI - Risk register page
 * Covers viewing a defendant's risk register with active and inactive registrations.
 */
public class TS_RefactorRiskRegisterPageSimulation extends Simulation implements PacSimulation {

    private static final FeederBuilder<String> courtFeeder = csv("data/courts.csv" ).random();
    private static final FeederBuilder<String> hearingIdFeeder = csv("data/hearingIds.csv").random(); // Need data/hearingIds.csv
    private static final FeederBuilder<String> defendantIdFeeder = csv("data/defendantIds.csv").random(); // Need data/defendantIds.csv

    // Scenario: View risk register page
    private static final ChainBuilder viewRiskRegisterPage = exec(
            http("View Risk Register Page" )
                    .get("/#{CourtCode}/hearing/#{hearingId}/defendant/#{defendantId}/risk")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Risk Register")) // Assuming a main heading for the page
    );

    // Scenario: View Active Registrations Tab
    // Assuming the active tab is the default view or accessed via a specific URL parameter/fragment
    private static final ChainBuilder viewActiveRegistrationsTab = exec(
            http("View Active Registrations Tab" )
                    .get("/#{CourtCode}/hearing/#{hearingId}/defendant/#{defendantId}/risk?tab=active") // Example with query param
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h2").is("Active Registrations")) // Assuming a heading for the active tab content
    );

    // Scenario: View Inactive Registrations Tab
    private static final ChainBuilder viewInactiveRegistrationsTab = exec(
            http("View Inactive Registrations Tab" )
                    .get("/#{CourtCode}/hearing/#{hearingId}/defendant/#{defendantId}/risk?tab=inactive") // Example with query param
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h2").is("Inactive Registrations")) // Assuming a heading for the inactive tab content
    );

    ScenarioBuilder scenario = scenario("PACFS UI - Risk Register Page")
            .exec(authenticate)
            .pause(pause)
            .feed(courtFeeder)
            .feed(hearingIdFeeder)
            .feed(defendantIdFeeder)
            .exec(viewRiskRegisterPage)
            .pause(pause)
            .exec(viewActiveRegistrationsTab)
            .pause(pause)
            .exec(viewInactiveRegistrationsTab);

    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol );
    }
}
