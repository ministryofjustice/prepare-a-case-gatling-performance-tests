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
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Requirement 10: PACFS UI - Probation record page
 * Covers viewing a defendant's complete probation record and navigating to related details.
 */
public class TS_RefactorProbationRecordPageSimulation extends Simulation implements PacSimulation {

    private static final FeederBuilder<String> courtFeeder = csv("data/courts.csv" ).random();
    private static final FeederBuilder<String> hearingIdFeeder = csv("data/hearingIds.csv").random(); // Need data/hearingIds.csv
    private static final FeederBuilder<String> defendantIdFeeder = csv("data/defendantIds.csv").random(); // Need data/defendantIds.csv

    // Placeholder feeders for convictionId and breachId
    private static final List<Map<String, Object>> convictionIdData = Arrays.asList(
            Collections.singletonMap("convictionId", "CONV123"),
            Collections.singletonMap("convictionId", "CONV456")
    );
    private static final FeederBuilder<Object> convictionIdFeeder = listFeeder(convictionIdData).random();

    private static final List<Map<String, Object>> breachIdData = Arrays.asList(
            Collections.singletonMap("breachId", "BREACH789"),
            Collections.singletonMap("breachId", "BREACH012")
    );
    private static final FeederBuilder<Object> breachIdFeeder = listFeeder(breachIdData).random();

    // Scenario: View probation record page
    private static final ChainBuilder viewProbationRecordPage = exec(
            http("View Probation Record Page" )
                    .get("/#{CourtCode}/hearing/#{hearingId}/defendant/#{defendantId}/record")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Probation Record")) // Assuming a main heading for the page
    );

    // Scenario: View Individual Order/Conviction
    private static final ChainBuilder viewIndividualConviction = exec(
            http("View Individual Conviction" )
                    .get("/#{CourtCode}/hearing/#{hearingId}/defendant/#{defendantId}/record/#{convictionId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Conviction Details")) // Assuming a heading for conviction details
    );

    // Scenario: View Breach Details
    private static final ChainBuilder viewBreachDetails = exec(
            http("View Breach Details" )
                    .get("/#{CourtCode}/hearing/#{hearingId}/defendant/#{defendantId}/record/#{convictionId}/breach/#{breachId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Breach Details")) // Assuming a heading for breach details
    );

    // Scenario: View Licence Conditions
    private static final ChainBuilder viewLicenceConditions = exec(
            http("View Licence Conditions" )
                    .get("/#{CourtCode}/hearing/#{hearingId}/defendant/#{defendantId}/record/#{convictionId}/licence-details")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Licence Conditions")) // Assuming a heading for licence conditions
    );

    ScenarioBuilder scenario = scenario("PACFS UI - Probation Record Page")
            .exec(authenticate)
            .pause(pause)
            .feed(courtFeeder)
            .feed(hearingIdFeeder)
            .feed(defendantIdFeeder)
            .exec(viewProbationRecordPage)
            .pause(pause)
            .feed(convictionIdFeeder)
            .exec(viewIndividualConviction)
            .pause(pause)
            .feed(breachIdFeeder)
            .exec(viewBreachDetails)
            .pause(pause)
            .exec(viewLicenceConditions);

    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol );
    }
}
