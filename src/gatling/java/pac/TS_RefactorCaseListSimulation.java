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
 * Requirement 4: PACFS UI - Case list
 * Covers viewing and managing the list of cases for a court.
 */
public class TS_RefactorCaseListSimulation extends Simulation implements PacSimulation {

    private static final FeederBuilder<String> courtFeeder = csv("data/courts.csv").random();
    private static final FeederBuilder<String> caseNoFeeder = csv("data/caseNo.csv").random();
    private static final FeederBuilder<String> hearingIdFeeder = csv("data/hearingIds.csv").random();
    private static final FeederBuilder<String> defendantIdFeeder = csv("data/defendantIds.csv").random();

    // Correct way to define an in-memory feeder in Gatling Java DSL:
    // Create a List of Maps where the key matches the variable name in the template
    private static final List<Map<String, Object>> prepStatusData = Arrays.asList(
            Collections.singletonMap("prepStatus", "READY"),
            Collections.singletonMap("prepStatus", "NOT_READY"),
            Collections.singletonMap("prepStatus", "IN_PROGRESS")
    );
    private static final FeederBuilder<Object> prepStatusFeeder = listFeeder(prepStatusData).random();

    private static final List<Map<String, Object>> probationStatusData = Arrays.asList(
            Collections.singletonMap("probationStatus", "NEW"),
            Collections.singletonMap("probationStatus", "ACTIVE"),
            Collections.singletonMap("probationStatus", "CLOSED")
    );
    private static final FeederBuilder<Object> probationStatusFeeder = listFeeder(probationStatusData).random();

    private static final List<Map<String, Object>> sessionData = Arrays.asList(
            Collections.singletonMap("session", "MORNING"),
            Collections.singletonMap("session", "AFTERNOON")
    );
    private static final FeederBuilder<Object> sessionFeeder = listFeeder(sessionData).random();


    // Scenario: View case list
    private static final ChainBuilder viewCaseList = exec(
            http("View Case List Page")
                    .get("/#{CourtCode}/cases")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: View case list with date parameter (as per developer notes)
    private static final ChainBuilder viewCaseListWithDate = exec(
            http("View Case List Page with Date")
                    .get("/#{CourtCode}/cases?date=2026-03-01") // Example date in the future
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Searching from cases list
    // Uses a placeholder query for now. In a real test, this might come from a feeder.
    private static final ChainBuilder searchCases = exec(
            http("Search Cases")
                    .get(AUTH_URL + "/search?query=07") // testCaseNumber: Using AUTH_URL as a base for generic search, adjust if needed
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Paging through cases
    private static final ChainBuilder pageThroughCases = exec(
            http("Page Through Cases - Page 2")
                    .get("/#{CourtCode}/cases?page=2") // Simulate navigating to page 2
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Setting admin prep status
    // Requires dynamic hearingId and defendantId, and a prep status.
    // For this example, we\'ll feed these from simple feeders. You\'d need to create data/hearingIds.csv and data/defendantIds.csv
    private static final ChainBuilder setAdminPrepStatus = exec(
            http("Set Admin Prep Status")
                    .put(AUTH_URL + "/hearing/#{hearingId}/defendants/#{defendantId}/prep-status/#{prepStatus}") // Using AUTH_URL as a base, adjust if needed
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200)) // Assuming 200 for successful update
    );

    ChainBuilder createHearingNote =
            exec(
                    http("POST Create Hearing Note")
                            .post("/hearing/#{hearingId}/defendants/#{defendantId}/notes")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Content-Type", "application/json")
                            .body(StringBody(
                                    "{\n" +
                                            "  \"hearingId\": \"#{hearingId}\",\n" +
                                            "  \"note\": \"Performance test note for defendant #{defendantId}\",\n" +
                                            "  \"author\": \"PerformanceTestUser\"\n" +
                                            "}"
                            ))
                            .asJson()
                            .check(
                                    status().in(200, 201),
                                    responseTimeInMillis().lte(2000)
                            )
            );

    ChainBuilder createCaseComment =
            exec(
                    http("POST /cases/{caseId}/defendants/{defendantId}/comments")
                            .post("/cases/#{caseId}/defendants/#{defendantId}/comments")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Content-Type", "application/json")
                            .body(StringBody(
                                    "{\n" +
                                            "  \"caseId\": \"#{caseId}\",\n" +
                                            "  \"comment\": \"Performance test comment for defendant #{defendantId}\",\n" +
                                            "  \"author\": \"PerfTestUser\"\n" +
                                            "}"
                            ))
                            .asJson()
                            .check(
                                    status().in(200, 201),
                                    responseTimeInMillis().lte(2000)
                            )
            );

    ChainBuilder getExtendedCourtCase =
            exec(
                    http("GET /hearing/{hearingId}/case/{courtCaseId}")
                            .get("/hearing/#{hearingId}/case/#{courtCaseId}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .check(
                                    status().is(200),
                                    jsonPath("$.id").exists(),
                                    responseTimeInMillis().lte(2000)
                            )
            );

    ChainBuilder getCourtCases =
            exec(
                    http("GET /court/{courtCode}/cases - filtered")
                            .get("/court/#{courtCode}/cases")
                            .queryParam("date", "#{hearingDate}")
                            .queryParam("breach", "true")
                            .queryParam("hearingStatus", "HEARD")
                            .queryParam("probationStatus", probationStatusFeeder)
                            .queryParam("source", "LIBRA")
                            .queryParam("session", sessionFeeder)
                            .queryParam("caseId", "#{courtCaseId}") // if you add feeder
                            .queryParam("hearingId", "#{hearingId}")
                            .queryParam("defendantId", "#{defendantId}")
                            .queryParam("page", "0")
                            .queryParam("size", "20")
                            .header("Authorization", "Bearer #{accessToken}")
                            .check(
                                    status().is(200),
                                    jsonPath("$.content").exists(),
                                    responseTimeInMillis().lte(3000)
                            )
            );

    ChainBuilder getCourtCasesMatcher =
            exec(
                    http("GET /court/{courtCode}/cases/matcher")
                            .get("/court/#{courtCode}/cases/matcher")
                            .queryParam("probationStatus", probationStatusFeeder)
                            .queryParam("session", sessionData)
                            .queryParam("source", "LIBRA")
                            .queryParam("breach", "true")
                            .queryParam("recentlyAdded", "true")
                            .queryParam("hearingStatus", "HEARD")
                            .queryParam("date", "#{hearingDate}")
                            .queryParam("page", "0")
                            .queryParam("size", "20")
                            .queryParam("hearingId", "#{hearingId}")
                            .queryParam("defendantId", "#{defendantId}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .check(
                                    status().is(200),
                                    jsonPath("$.content").exists(),
                                    jsonPath("$.content[*].hearingId").exists(),
                                    jsonPath("$.content[*].defendantId").exists(),
                                    responseTimeInMillis().lte(3000)
                            )
            );

    ChainBuilder getCourtCaseByCaseNo =
            exec(
                    http("GET /court/{courtCode}/case/{caseNo}")
                            .get("/court/#{courtCode}/case/#{caseNo}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .check(
                                    status().is(200),
                                    jsonPath("$.caseNo").exists(),
                                    responseTimeInMillis().lte(3000)
                            )
            );


    ScenarioBuilder scenario = scenario("PACFS UI - Case List")
            .feed(hearingIdFeeder)
            .feed(defendantIdFeeder)
            .feed(prepStatusFeeder)
            .feed(caseNoFeeder)
            .exec(authenticate)
            .pause(pause)
            .feed(courtFeeder) // Feed court code for subsequent requests
            .exec(viewCaseList)
            .pause(pause)
            .exec(viewCaseListWithDate)
            .pause(pause)
            .exec(searchCases)
            .pause(pause)
            .exec(pageThroughCases)
            .pause(pause)
            .exec(setAdminPrepStatus)


            .pause(pause)
            .exec(createHearingNote)
            .pause(pause)
            .exec(createCaseComment)
            .pause(pause)
            .exec(getExtendedCourtCase)
            .pause(pause)
            .exec(session ->
                    session.set("hearingDate", java.time.LocalDate.now().toString())
            )
            .pause(pause)
            .exec(getCourtCases)
            .pause(pause)
            .exec(getCourtCasesMatcher)
            .pause(pause)
            .exec(getCourtCaseByCaseNo);


    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol);
    }
}
