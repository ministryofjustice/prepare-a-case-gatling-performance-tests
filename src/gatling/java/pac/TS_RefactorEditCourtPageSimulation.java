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
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Requirement 3: PACFS UI - Edit courts page
 * Covers managing courts: searching, adding, saving, canceling, and removing.
 */
public class TS_RefactorEditCourtPageSimulation extends Simulation implements PacSimulation {

    // Define the feeder to read court codes from the data file
    private static final FeederBuilder<String> courtFeeder = csv("data/courts.csv").random();
    private static final FeederBuilder<String> caseFeeder = csv("data/caseIds.csv").random();
    private static final FeederBuilder<String> hearingFeeder = csv("data/hearingIds.csv").random();
    private static final FeederBuilder<String> defendantFeeder = csv("data/defendantIds.csv").random();

    private static final List<Map<String, Object>> sessionData = Arrays.asList(
            Collections.singletonMap("session", "MORNING"),
            Collections.singletonMap("session", "AFTERNOON")
    );
    private static final FeederBuilder<Object> sessionFeeder = listFeeder(sessionData).random();

    private static final List<Map<String, Object>> probationStatusData = Arrays.asList(
            Collections.singletonMap("probationStatus", "NEW"),
            Collections.singletonMap("probationStatus", "ACTIVE"),
            Collections.singletonMap("probationStatus", "CLOSED"),
            Collections.singletonMap("probationStatus", "Pre-sentence record")
    );
    private static final FeederBuilder<Object> probationStatusFeeder = listFeeder(probationStatusData).random();


    // Scenario: Searching for a court
    ChainBuilder searchForCourt = exec(
            http("Search for Court")
                    // .feed(courtFeeder) // Use a court from the feeder as a search query
                    .get("/my-courts/search?query=#{CourtCode}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200)) // Assuming successful search returns 200
    );


    ChainBuilder searchCasesForCourt =
            exec(
                    http("Search Cases For Court")
                            .get("/court/#{CourtCode}/cases")
                            .header("Authorization", "Bearer #{accessToken}")
                            .queryParam("date", "#{hearingDate}")
                            .queryParam("probationStatus", probationStatusFeeder)
                            .queryParam("courtRoom", "#{CourtCode}")
                            .queryParam("session", sessionFeeder)
                            .queryParam("source", "LIBRA")
                            .queryParam("breach", true)
                            .queryParam("recentlyAdded", true)
                            .queryParam("hearingStatus", "HEARD")
                            .queryParam("page", 0)
                            .queryParam("size", 0)
                            .queryParam("numberOfPossibleMatches", 0)
                            .queryParam("forename", "performance-test")
                            .queryParam("surname", "performance-test")
                            .queryParam("defendantName", "performance-test")
                            .queryParam("caseId", "#{caseId}")
                            .queryParam("hearingId", "#{hearingId}")
                            .queryParam("defendantId", "#{defendantId}")
                            .queryParam("hearingOutcomeNotRequired", true)
                            .check(status().is(200))
            );


    ChainBuilder upSearchHearingById =
            exec(
                    http("Upsert Hearing By Id")
                            .put("/hearing/#{hearingId}")   // use .post(...) if Swagger says POST
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Content-Type", "application/json")

                            .body(StringBody(
                                    "{\n" +
                                            "  \"caseNo\": \"CASE-12345\",\n" +
                                            "  \"caseId\": \"#{caseId}\",\n" +
                                            "  \"hearingId\": \"#{hearingId}\",\n" +
                                            "  \"source\": \"LIBRA\",\n" +
                                            "  \"urn\": \"URN-123\",\n" +
                                            "  \"hearingDays\": [\n" +
                                            "    {\n" +
                                            "      \"courtCode\": \"#{CourtCode}\",\n" +
                                            "      \"courtRoom\": \"1\",\n" +
                                            "      \"sessionStartTime\": \"2026-02-22T03:24:17.747Z\"\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"defendants\": [\n" +
                                            "    {\n" +
                                            "      \"defendantId\": \"#{defendantId}\",\n" +
                                            "      \"name\": {\n" +
                                            "        \"title\": \"Mr\",\n" +
                                            "        \"forename1\": \"John\",\n" +
                                            "        \"surname\": \"Doe\"\n" +
                                            "      },\n" +
                                            "      \"dateOfBirth\": \"1985-06-12\",\n" +
                                            "      \"probationStatus\": \"" + probationStatusFeeder + "\",\n" +
                                            "      \"type\": \"ORGANISATION\"\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"hearingEventType\": \"COURT_HEARING\",\n" +
                                            "  \"hearingType\": \"TRIAL\",\n" +
                                            "  \"listNo\": \"LIST-1\"\n" +
                                            "}"
                            )).asJson()
                            .check(status().in(200, 201))
            );

    // Scenario: Adding a court
    ChainBuilder addCourt = exec(
            http("Add Court (Temporary )")
                    // .feed(courtFeeder) // Select a court to add
                    .post("/my-courts/add")
                    .header("Authorization", "Bearer #{accessToken}")
                    .formParam("court", "#{CourtCode}")
                    .check(status().is(200)) // Assuming successful temporary add returns 200
    );

    // Scenario: Saving my courts
    ChainBuilder saveCourts = exec(
            http("Save Courts List")
                    .put("/my-courts/save") // Assuming a PUT endpoint for saving
                    .header("Authorization", "Bearer #{accessToken}")
                    // In a real scenario, you might need to pass the list of courts in the body
                    .check(status().is(200)) // Assuming successful save returns 200
    ).exec(http("Redirect to My Courts Page after Save")
            .get("/my-courts")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().is(200))
    );

    // Scenario: Cancel button
    ChainBuilder cancelChanges = exec(
            http("Cancel Changes")
                    .get("/my-courts") // Simply navigate back to /my-courts
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Removing a court
    ChainBuilder removeCourt = exec(
            http("Remove Court (Temporary )")
                    // .feed(courtFeeder) // Select a court to remove
                    .post("/my-courts/remove")
                    .header("Authorization", "Bearer #{accessToken}")
                    .formParam("court", "#{CourtCode}")
                    .check(status().is(200)) // Assuming successful temporary remove returns 200
    );

    ChainBuilder getHearingByDefendant =
            exec(
                    http("Get Hearing By Defendant Id")
                            .get("/hearing/#{hearingId}/defendant/#{defendantId}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            .check(
                                    status().is(200),
                                    responseTimeInMillis().lte(2000)
                                    //jsonPath("$.hearingId").is("#{hearingId}")
                            )
            );

    ChainBuilder toggleHearingOutcomeRequired =
            exec(
                    http("Toggle Hearing Outcome Required Status")
                            .put("/hearing/#{hearingId}/defendant/#{defendantId}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Content-Type", "application/json")
                            .body(StringBody(
                                    "{ \"hearingOutcomeNotRequired\": true }"
                            ))
                            .asJson()
                            .check(
                                    status().in(200, 204),
                                    responseTimeInMillis().lte(2000)
                            )
            );

    ChainBuilder getOffenderByDefendantId =
            exec(
                    http("GET Offender Details By Defendant Id")
                            .get("/defendant/#{defendantId}/offender")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            .check(
                                    status().is(200),
                                    //jsonPath("$.pnc").exists(),
                                    //jsonPath("$.crn").optional(),
                                    responseTimeInMillis().lte(2000)
                            )
            );

    ChainBuilder updateOffenderByDefendantId =
            exec(
                    http("PUT Update Offender By Defendant Id")
                            .put("/defendant/#{defendantId}/offender")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Content-Type", "application/json")
                            .body(StringBody(
                                    "{\n" +
                                            "  \"probationStatus\": \\" + probationStatusFeeder + "\",\n" +
                                            "  \"crn\": \"C16000\",\n" +
                                            "  \"pnc\": \"A/160000BA\",\n" +
                                            "  \"cro\": \"CRO\",\n" +
                                            "  \"previouslyKnownTerminationDate\": \"2026-02-22\",\n" +
                                            "  \"suspendedSentenceOrder\": true,\n" +
                                            "  \"breach\": true,\n" +
                                            "  \"preSentenceActivity\": true,\n" +
                                            "  \"awaitingPsr\": true\n" +
                                            "}"
                            ))
                            .asJson()
//                            .check(
//                                    status().in(200, 201),
//                                    responseTimeInMillis().lte(2000)
//                            )
            );

    ChainBuilder deleteOffenderByDefendantId =
            exec(
                    http("DELETE Offender By Defendant Id")
                            .delete("/defendant/#{defendantId}/offender")
                            .header("Authorization", "Bearer #{accessToken}")
                            .check(
                             //       status().in(200, 204, 404),
                                    responseTimeInMillis().lte(2000)
                            )
            );


    ScenarioBuilder scenario = scenario("PACFS UI - Edit Courts Page")
            .feed(courtFeeder) // Inject the feeder early in the scenario
            .feed(defendantFeeder)
            .feed(hearingFeeder)
            .feed(caseFeeder)
            .exec(authenticate)
            .pause(pause)
            .exec(searchForCourt)
            .pause(pause)
            .exec(session ->
                    session.set("hearingDate", java.time.LocalDate.now().toString())
            )
            .exec(searchCasesForCourt)
            .pause(pause)
            .exec(upSearchHearingById)
            // .exec(searchHearingById)
            .pause(pause)
            .exec(getHearingByDefendant)
            .pause(pause)
            .exec(toggleHearingOutcomeRequired)
            .pause(pause)
            .exec(getOffenderByDefendantId)
            .pause(pause)
            .exec(updateOffenderByDefendantId)
            .pause(pause)
            .exec(deleteOffenderByDefendantId)

            .exec(addCourt)
            .pause(pause)
            .exec(saveCourts)
            .pause(pause)
            .exec(removeCourt)
            .pause(pause)
            .exec(cancelChanges);

    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol);
    }
}
