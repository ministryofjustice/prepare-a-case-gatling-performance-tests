package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Requirement 5: PACFS UI - Case list Filters
 * Covers filtering cases by date, multiple criteria, clearing filters, and tab-based filtering.
 */
public class TS_RefactorCaseListFiltersSimulation extends Simulation implements PacSimulation {

    private static final FeederBuilder<String> courtFeeder = csv("data/courts.csv").random();
    private static final FeederBuilder<String> hearingFeeder = csv("data/hearingIds.csv").random();
    private static final FeederBuilder<String> defendantFeeder = csv("data/defendantIds.csv").random();


    // Feeder for probation status
    private static final List<Map<String, Object>> probationStatusData = Arrays.asList(
            Collections.singletonMap("probationStatus", "NEW"),
            Collections.singletonMap("probationStatus", "ACTIVE"),
            Collections.singletonMap("probationStatus", "CLOSED")
    );
    private static final FeederBuilder<Object> probationStatusFeeder = listFeeder(probationStatusData).random();

    // Feeder for court room (example values)
    private static final List<Map<String, Object>> courtRoomData = Arrays.asList(
            Collections.singletonMap("courtRoom", "Room 1"),
            Collections.singletonMap("courtRoom", "Room 2"),
            Collections.singletonMap("courtRoom", "Room 3")
    );
    private static final FeederBuilder<Object> courtRoomFeeder = listFeeder(courtRoomData).random();

    // Feeder for session (example values)
    private static final List<Map<String, Object>> sessionData = Arrays.asList(
            Collections.singletonMap("session", "MORNING"),
            Collections.singletonMap("session", "AFTERNOON")
    );
    private static final FeederBuilder<Object> sessionFeeder = listFeeder(sessionData).random();

    // Feeder for source (example values)
    private static final List<Map<String, Object>> sourceData = Arrays.asList(
            Collections.singletonMap("source", "SOURCE_A"),
            Collections.singletonMap("source", "SOURCE_B")
    );
    private static final FeederBuilder<Object> sourceFeeder = listFeeder(sourceData).random();

    // Feeder for flag (example values)
    private static final List<Map<String, Object>> flagData = Arrays.asList(
            Collections.singletonMap("flag", "FLAG_X"),
            Collections.singletonMap("flag", "FLAG_Y")
    );
    private static final FeederBuilder<Object> flagFeeder = listFeeder(flagData).random();

    // Feeder for tab filters (example values)
    private static final List<Map<String, Object>> tabFilterData = Arrays.asList(
            Collections.singletonMap("tab", "All"),
            Collections.singletonMap("tab", "Today"),
            Collections.singletonMap("tab", "Upcoming")
    );
    private static final FeederBuilder<Object> tabFilterFeeder = listFeeder(tabFilterData).random();

    private static final List<Map<String, Object>> prepStatusData = Arrays.asList(
            Collections.singletonMap("prepStatus", "NOT_STARTED"),
            Collections.singletonMap("prepStatus", "IN_PROGRESS"),
            Collections.singletonMap("prepStatus", "COMPLETE"),
            Collections.singletonMap("prepStatus", "NOT_REQUIRED")
    );
    private static final FeederBuilder<Object> prepStatusFeeder = listFeeder(prepStatusData).random();

    private static final List<Map<String, Object>> hearingOutcomeData = Arrays.asList(
            Collections.singletonMap("hearingOutcomeType", "PROBATION_SENTENCE"),
            Collections.singletonMap("hearingOutcomeType", "ADJOURNED"),
            Collections.singletonMap("hearingOutcomeType", "DISMISSED")
    );
    private static final FeederBuilder<Object> hearingOutcomeFeeder = listFeeder(hearingOutcomeData).random();

    private static final List<Map<String, Object>> assignedUserData = Arrays.asList(
            Collections.singletonMap("assignedTo", "user1"),
            Collections.singletonMap("assignedTo", "user2"),
            Collections.singletonMap("assignedTo", "court.officer"),
            Collections.singletonMap("assignedTo", "probation.manager")
    );
    private static final FeederBuilder<Object> assignedUserFeeder = listFeeder(assignedUserData).random();


    // Scenario: Filtering cases by date
    private static final ChainBuilder filterByDate = exec(
            http("Filter Cases by Date")
                    .get("/#{CourtCode}/cases?date=" + LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE)) // Example: 7 days in the future
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Filtering cases by multiple criteria
    private static final ChainBuilder applyMultipleFilters = exec(
            http("Apply Multiple Filters")
                    .get("/#{CourtCode}/cases?probationStatus=#{probationStatus}&courtRoom=#{courtRoom}&session=#{session}&source=#{source}&flag=#{flag}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Clearing filters
    private static final ChainBuilder clearFilters = exec(
            http("Clear All Filters")
                    .get("/#{CourtCode}/cases") // Navigating to base URL clears filters
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Filtering cases by tabs
    private static final ChainBuilder filterByTab = exec(
            http("Filter Cases by Tab")
                    .get("/#{CourtCode}/cases?tab=#{tab}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    ChainBuilder updatePrepStatus =
            exec(
                    http("PUT /hearing/{hearingId}/defendants/{defendantId}/prep-status/{prepStatus}")
                            .put("/hearing/#{hearingId}/defendants/#{defendantId}/prep-status/#{prepStatus}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .check(
                                    status().in(200, 204),
                                    jsonPath("$.prepStatus").isEL("#{prepStatus}"),
                                    responseTimeInMillis().lte(2000)
                            )
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


    ScenarioBuilder scenario = scenario("PACFS UI - Case List Filters")
            .feed(probationStatusFeeder)
            .feed(courtRoomFeeder)
            .feed(sessionFeeder)
            .feed(sourceFeeder)
            .feed(courtFeeder)
            .feed(prepStatusFeeder)
            .feed(hearingOutcomeFeeder)
            .feed(assignedUserFeeder)
            .feed(flagFeeder)
            .feed(hearingFeeder)
            .feed(defendantFeeder)
            .pause(pause)
            .exec(authenticate)
            .pause(pause)
            .feed(courtFeeder)
            .exec(filterByDate)
            .pause(pause)
            .exec(applyMultipleFilters)
            .pause(pause)
            .exec(clearFilters)
            .pause(pause)
            .feed(tabFilterFeeder)
            .exec(filterByTab)

            .pause(pause)
            .exec(updatePrepStatus)
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
        ).protocols(httpProtocol);
    }
}
