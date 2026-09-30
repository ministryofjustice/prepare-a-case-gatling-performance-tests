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
 * Requirement 7: PACFS UI - Outcomes page
 * Covers viewing and managing a list of case outcomes.
 */
public class TS_RefactorOutcomePageSimulation extends Simulation implements PacSimulation {

    private static final FeederBuilder<String> courtFeeder = csv("data/courts.csv" ).random();
    private static final FeederBuilder<String> hearingFeeder = csv("data/hearingIds.csv").random();
    private static final FeederBuilder<String> defendantFeeder = csv("data/defendantIds.csv").random();


    // Feeder for tab filters
    private static final List<Map<String, Object>> tabFilterData = Arrays.asList(
            Collections.singletonMap("tab", "in progress"),
            Collections.singletonMap("tab", "cases to result"),
            Collections.singletonMap("tab", "resulted cases")
    );
    private static final FeederBuilder<Object> tabFilterFeeder = listFeeder(tabFilterData).random();

    // Placeholder feeders for dynamic data like case IDs and outcome types
    // You would need to create these CSV files (e.g., data/caseIds.csv) with appropriate data.
    private static final FeederBuilder<String> caseIdFeeder = csv("data/caseIds.csv").random();

    // Define the data list
    private static final List<Map<String, Object>> outcomeTypeData = Arrays.asList(
            Collections.singletonMap("outcomeType", "PROBATION_OUTCOME"),
            Collections.singletonMap("outcomeType", "CUSTODY_OUTCOME"),
            Collections.singletonMap("outcomeType", "COMMUNITY_OUTCOME")
    );

    // Create the feeder using listFeeder
    private static final FeederBuilder<Object> outcomeTypeFeeder = listFeeder(outcomeTypeData).random();

    // Scenario: View outcomes case list
    private static final ChainBuilder viewOutcomesList = exec(
            http("View Outcomes List Page" )
                    .get("/#{CourtCode}/outcomes")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Filtering the outcomes case list using tabs
    private static final ChainBuilder filterOutcomesByTab = exec(
            http("Filter Outcomes by Tab" )
                    .get("/#{CourtCode}/outcomes?tab=#{tab}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Paging through outcomes case list
    private static final ChainBuilder pageThroughOutcomesList = exec(
            http("Page Through Outcomes List - Page 2" )
                    .get("/#{CourtCode}/outcomes?page=2")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Assigning a case to myself (simulated API call)
    // Assumes a POST request with a list of case IDs
    private static final ChainBuilder assignCaseToMyself = exec(
            http("Assign Case to Myself" )
                   // .post(AUTH_URL + "/api/outcomes/assign") // Placeholder API endpoint
                    .post("/api/outcomes/assign") // Placeholder API endpoint
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("{\"caseIds\": [\"#{caseId}\"]}")).asJson()
                    .check(status().is(200))
    );

    // Scenario: Set the result of a case (simulated API call)
    private static final ChainBuilder setCaseResult = exec(
            http("Set Case Result" )
                   // .put(AUTH_URL + "/api/outcomes/#{caseId}/result") // Placeholder API endpoint
                    .put("/api/outcomes/#{caseId}/result") // Placeholder API endpoint
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("{\"outcomeType\": \"#{outcomeType}\"}")).asJson()
                    .check(status().is(200))
    );

    // Scenario: Move a case to “resulted” (simulated API call)
    private static final ChainBuilder moveCaseToResulted = exec(
            http("Move Case to Resulted" )
                   // .put(AUTH_URL + "/api/outcomes/#{caseId}/move-to-resulted") // Placeholder API endpoint
                    .put("/api/outcomes/#{caseId}/move-to-resulted") // Placeholder API endpoint
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );


    ChainBuilder updateHearingOutcome =
            exec(
                    http("PUT /hearing/{hearingId}/defendant/{defendantId}/outcome")
                            .put("/hearing/#{hearingId}/defendant/#{defendantId}/outcome")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Content-Type", "application/json")
                            .body(StringBody(
                                    "{\n" +
                                            "  \"hearingOutcomeType\": \"#{hearingOutcomeType}\"\n" +
                                            "}"
                            ))
                            .asJson()
                            .check(
                                    status().in(200, 201, 204),
                                    responseTimeInMillis().lte(2000)
                            )
            );

    ChainBuilder assignHearingOutcome =
            exec(
                    http("PUT /hearing/{hearingId}/defendant/{defendantId}/outcome/assign")
                            .put("/hearing/#{hearingId}/defendant/#{defendantId}/outcome/assign")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Content-Type", "application/json")
                            .body(StringBody(
                                    "{\n" +
                                            "  \"assignedTo\": \"#{assignedTo}\"\n" +
                                            "}"
                            ))
                            .asJson()
                            .check(
                                    status().in(200, 204),
                                    jsonPath("$.assignedTo").isEL("#{assignedTo}"),
                                    responseTimeInMillis().lte(2000)
                            )
            );

    ChainBuilder resultHearingOutcome =
            exec(
                    http("POST /hearing/{hearingId}/defendant/{defendantId}/outcome/result")
                            .post("/hearing/#{hearingId}/defendant/#{defendantId}/outcome/result")
                            .header("Authorization", "Bearer #{accessToken}")
                            .check(
                                    status().in(200, 201, 204),
                                    jsonPath("$.status").exists(),
                                    responseTimeInMillis().lte(3000)
                            )
            );

    // ChainBuilder for GET /hearing-outcome-types
    ChainBuilder getHearingOutcomeTypes =
            exec(
                    http("GET Hearing Outcome Types")
                            .get("/hearing-outcome-types")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            .check(
                                    status().is(200),
                                    jsonPath("$").exists(), // Ensure JSON array returned
                                    responseTimeInMillis().lte(2000)
                            )
            );

    // ChainBuilder for GET /courts/{courtCode}/hearing-outcomes
    ChainBuilder getHearingOutcomes =
            exec(
                    http("GET Hearing Outcomes")
                            .get("/courts/#{courtCode}/hearing-outcomes")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Query Parameters
                            .queryParam("state", "RESULTED") // can be NEW, IN_PROGRESS, RESULTED
                            .queryParam("outcomeType", "PROBATION_SENTENCE,NON_PROBATION_SENTENCE") // multiple values comma-separated
                            .queryParam("sortBy", "HEARING_DATE")
                            .queryParam("order", "ASC")
                            .queryParam("courtRoom", "1") // you can feed this dynamically if needed
                            //.queryParam("assignedToUuid", "#{assignedToUuid}") // optional if you have UUIDs
                            .queryParam("page", 0)
                            .queryParam("size", 10)
                            // Checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists())
                            .check(responseTimeInMillis().lte(2000))
            );

    ScenarioBuilder scenario = scenario("PACFS UI - Outcomes Page")
            .feed(hearingFeeder)
            .feed(courtFeeder)
            .feed(defendantFeeder)
            .exec(authenticate)
            .pause(pause)
            .exec(viewOutcomesList)
            .pause(pause)
            .feed(tabFilterFeeder)
            .exec(filterOutcomesByTab)
            .pause(pause)
            .exec(pageThroughOutcomesList)
            .pause(pause)
            .feed(caseIdFeeder) // Feed a case ID for assignment/result/move
            .exec(assignCaseToMyself)
            .pause(pause)
            .feed(outcomeTypeFeeder)
            .exec(setCaseResult)
            .pause(pause)
            .exec(moveCaseToResulted)

            .pause(pause)
            .exec(updateHearingOutcome)
            .pause(pause)
            .exec(assignHearingOutcome)
            .pause(pause)
            .exec(resultHearingOutcome)
            .pause(pause)
            .exec(getHearingOutcomeTypes)
            .pause(pause)
            .exec(getHearingOutcomes);

    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol );
    }
}
