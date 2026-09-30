package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static io.gatling.javaapi.http.HttpDsl.header;


/**
 * Requirement 2: PACFS UI - My courts page
 * Covers viewing assigned courts, viewing cases for a court, and editing courts.
 */
public class TS_RefactorMyCourtPageSimulation extends Simulation implements PacSimulation {

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
            Collections.singletonMap("probationStatus", "CLOSED")
    );
    private static final FeederBuilder<Object> probationStatusFeeder = listFeeder(probationStatusData).random();

    private static final List<Map<String, Object>> courtRoomData = Arrays.asList(
            Collections.singletonMap("courtRoom", "Room 1"),
            Collections.singletonMap("courtRoom", "Room 2"),
            Collections.singletonMap("courtRoom", "Room 3")
    );
    private static final FeederBuilder<Object> courtRoomFeeder = listFeeder(courtRoomData).random();

    private static final List<String> allowedCourtCodes;
    static {
        try {
            allowedCourtCodes = Files.lines(
                            Paths.get("src/gatling/resources/data/courts.csv"))
                    .skip(1) // skip header
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load courts.csv", e);
        }
    }



    // Scenario: Viewing court Lists
    ChainBuilder viewMyCourts = exec(
            http("View Courts Lists")
                    .get("/courts")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(header("Content-Type").is("text/html;charset=UTF-8"))
                    .check(bodyString().saveAs("responseBody"))
    )
            .exec(session -> {

                String body = session.getString("responseBody");
                System.out.println("Response body length: " + body.length());

                return session;
            });

    ChainBuilder viewCasesForCourt =
            exec(
                    http("View Cases for Selected Court")
                            .get("/court/#{CourtCode}/cases")
                            .header("Authorization", "Bearer #{accessToken}")
                            .queryParam("date", "#{hearingDate}")
                            .queryParam("probationStatus", probationStatusFeeder)
                            .queryParam("courtRoom", courtRoomFeeder)
                            .queryParam("session", sessionFeeder)
                            .queryParam("source", "LIBRA")//COMMON_PLATFORM
                            .queryParam("breach", true)
                            .queryParam("recentlyAdded", true)
                            .queryParam("hearingOutcomeNotRequired", true)
                            .queryParam("hearingStatus", "HEARD")
                            .queryParam("page", 0)
                            .queryParam("size", 10)
                            .queryParam("numberOfPossibleMatches", 0)
                            .queryParam("forename", "Performance Test")
                            .queryParam("surname", "Performance Test")
                            .queryParam("defendantName", "Performance Test")
                            //.queryParam("caseId", "#{caseId}")
                            //.queryParam("hearingId", "#{hearingId}")
                            //.queryParam("defendantId", "#{defendantId}")
                            .check(status().is(200)));


    // Scenario: Link to Edit my courts
    ChainBuilder editMyCourts = exec(session -> {
        String selectedCourt = session.getString("CourtCode");
        System.out.println("Selected CourtCode: " + selectedCourt);
        return session;
    })
            .exec(
                    http("Edit My Courts Page")
                            .put("/courts/#{CourtCode}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .body(StringBody(session -> {

                                String now = Instant.now().toString();
                                return "{\n" +
                                        "  \"created\": \"" + now + "\",\n" +
                                        "  \"lastUpdated\": \"" + now + "\",\n" +
                                        "  \"createdBy\": \"performance-test\",\n" +
                                        "  \"lastUpdatedBy\": \"performance-test\",\n" +
                                        "  \"deleted\": false,\n" +
                                        "  \"version\": 0,\n" +
                                        "  \"name\": \"Performance Court\",\n" +
                                        "  \"courtCode\": \"" + courtCode + "\"\n" +
                                        // "  \"courtCode\": \"#{CourtCode}\n" +
                                        "}";
                            })).asJson()
                            .check(status().in(200, 201, 204)) // allow common success codes
                            .check(status().is(200))
            );


    ScenarioBuilder scenario = scenario("PACFS UI - My Courts Page")
            .feed(courtFeeder) // Inject the feeder early in the scenario
            .feed(caseFeeder)
            .feed(defendantFeeder)
            .feed(hearingFeeder)
            .exec(authenticate)
            .pause(pause)
            .exec(viewMyCourts
                    .pause(pause)
                    .exec(session ->
                            session.set("hearingDate", java.time.LocalDate.now().toString())
                    )
                    .exec(viewCasesForCourt)
                    .pause(pause)
                    .exec(editMyCourts));
    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol);
    }
}
