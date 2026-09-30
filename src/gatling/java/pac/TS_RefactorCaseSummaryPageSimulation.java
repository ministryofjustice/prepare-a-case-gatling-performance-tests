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
 * Requirement 8: PACFS UI - Case summary page
 * Covers viewing comprehensive case summary, managing notes and comments, and performing actions.
 */
public class TS_RefactorCaseSummaryPageSimulation extends Simulation implements PacSimulation {

    private static final FeederBuilder<String> courtFeeder = csv("data/courts.csv").random();
    private static final FeederBuilder<String> hearingIdFeeder = csv("data/hearingIds.csv").random(); // Need data/hearingIds.csv
    private static final FeederBuilder<String> defendantIdFeeder = csv("data/defendantIds.csv").random(); // Need data/defendantIds.csv
    private static final FeederBuilder<String> crmFeeder = csv("data/courts.csv").random();

    // Feeder for new note content
    private static final List<Map<String, Object>> noteContentData = Arrays.asList(
            Collections.singletonMap("noteContent", "This is a new hearing note."),
            Collections.singletonMap("noteContent", "Another important observation.")
    );
    private static final FeederBuilder<Object> noteContentFeeder = listFeeder(noteContentData).random();

    // Feeder for new comment content
    private static final List<Map<String, Object>> commentContentData = Arrays.asList(
            Collections.singletonMap("commentContent", "Adding a new comment to the case."),
            Collections.singletonMap("commentContent", "Follow-up required on this point.")
    );
    private static final FeederBuilder<Object> commentContentFeeder = listFeeder(commentContentData).random();

    // Feeder for outcome types (reused from Requirement 7)
    private static final List<Map<String, Object>> outcomeTypeData = Arrays.asList(
            Collections.singletonMap("outcomeType", "PROBATION_OUTCOME"),
            Collections.singletonMap("outcomeType", "CUSTODY_OUTCOME"),
            Collections.singletonMap("outcomeType", "COMMUNITY_OUTCOME")
    );
    private static final FeederBuilder<Object> outcomeTypeFeeder = listFeeder(outcomeTypeData).random();

    // Feeder for convictionId types (reused from Requirement 7)
    private static final List<Map<String, Object>> convictionIdTypeData = Arrays.asList(
            Collections.singletonMap("convictionId", "111"),
            Collections.singletonMap("convictionId", "222"),
            Collections.singletonMap("convictionId", "333")
    );
    private static final FeederBuilder<Object> convictionIdTypeFeeder = listFeeder(outcomeTypeData).random();

    // Feeder for convictionId types (reused from Requirement 7)
    private static final List<Map<String, Object>> breachIdTypeData = Arrays.asList(
            Collections.singletonMap("breachId", "111"),
            Collections.singletonMap("breachId", "222"),
            Collections.singletonMap("breachId", "333")
    );
    private static final FeederBuilder<Object> breachIdTypeFeeder = listFeeder(outcomeTypeData).random();


    // Scenario: View case summary page
    private static final ChainBuilder viewCaseSummaryPage = exec(
            http("View Case Summary Page")
                    .get("/#{CourtCode}/hearing/#{hearingId}/defendant/#{defendantId}/summary")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Case Summary")) // Assuming a main heading for the page
    );

    // Scenario: Add hearing note
    private static final ChainBuilder addHearingNote = exec(
            http("Add Hearing Note")
                    .post(AUTH_URL + "/api/hearing/#{hearingId}/notes") // Placeholder API endpoint
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("{\"content\": \"#{noteContent}\"}")).asJson()
                    .check(status().is(201)) // Assuming 201 Created for successful addition
    );

    // Scenario: Edit hearing note (requires a note ID, which would be extracted dynamically in a real test)
    private static final ChainBuilder editHearingNote = exec(
            http("Edit Hearing Note")
                    .put(AUTH_URL + "/api/hearing/#{hearingId}/notes/123") // Placeholder note ID
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("{\"content\": \"#{noteContent} - EDITED\"}")).asJson()
                    .check(status().is(200))
    );

    // Scenario: Delete hearing note (requires a note ID)
    private static final ChainBuilder deleteHearingNote = exec(
            http("Delete Hearing Note")
                    .delete(AUTH_URL + "/api/hearing/#{hearingId}/notes/123") // Placeholder note ID
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(204)) // Assuming 204 No Content for successful deletion
    );

    // Scenario: Add comment
    private static final ChainBuilder addComment = exec(
            http("Add Comment")
                    .post(AUTH_URL + "/api/defendant/#{defendantId}/comments") // Placeholder API endpoint
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("{\"content\": \"#{commentContent}\"}")).asJson()
                    .check(status().is(201))
    );

    // Scenario: Edit comment (requires a comment ID)
    private static final ChainBuilder editComment = exec(
            http("Edit Comment")
                    .put(AUTH_URL + "/api/defendant/#{defendantId}/comments/456") // Placeholder comment ID
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("{\"content\": \"#{commentContent} - EDITED\"}")).asJson()
                    .check(status().is(200))
    );

    // Scenario: Delete comment (requires a comment ID)
    private static final ChainBuilder deleteComment = exec(
            http("Delete Comment")
                    .delete(AUTH_URL + "/api/defendant/#{defendantId}/comments/456") // Placeholder comment ID
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(204))
    );

    // Scenario: Assigning a case to myself (reused from Requirement 7)
    private static final ChainBuilder assignCaseToMyself = exec(
            http("Assign Case to Myself (from Case Summary )")
                    .post(AUTH_URL + "/api/outcomes/assign") // Placeholder API endpoint
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("{\"caseIds\": [\"#{defendantId}\"]}")).asJson() // Assuming defendantId can be used as caseId for assignment
                    .check(status().is(200))
    );

    // Scenario: Move a case to “resulted” (reused from Requirement 7)
    private static final ChainBuilder moveCaseToResulted = exec(
            http("Move Case to Resulted (from Case Summary )")
                    .put(AUTH_URL + "/api/outcomes/#{defendantId}/move-to-resulted") // Placeholder API endpoint
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
    );

    // Scenario: Set the result of a case (reused from Requirement 7)
    private static final ChainBuilder setCaseResult = exec(
            http("Set Case Result (from Case Summary )")
                    .put(AUTH_URL + "/api/outcomes/#{defendantId}/result") // Placeholder API endpoint
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("{\"outcomeType\": \"#{outcomeType}\"}")).asJson()
                    .check(status().is(200))
    );


    // ChainBuilder for GET /offender/{crn}/registrations
    ChainBuilder getOffenderRegistrations =
            exec(
                    http("GET Offender Registrations")
                            .get("/offender/#{crn}/registrations") // path param from feeder
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists()) // basic check for response body
                            .check(responseTimeInMillis().lte(2000))
            );

    // ChainBuilder for GET /offender/{crn}/probation-status-detail
    ChainBuilder getOffenderProbationStatusDetail =
            exec(
                    http("GET Offender Probation Status Detail")
                            .get("/offender/#{crn}/probation-status-detail") // path param from feeder
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists()) // ensure response body exists
                            .check(responseTimeInMillis().lte(2000))
            );

    // ChainBuilder for GET /offender/{crn}/probation-record
    ChainBuilder getOffenderProbationRecord =
            exec(
                    http("GET Offender Probation Record")
                            .get("/offender/#{crn}/probation-record") // Path param
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Optional query parameter (defaults to true)
                            .queryParam("applyDocTypeFilter", true)
                            // Checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists())
                            .check(responseTimeInMillis().lte(2000))
            );

    // ChainBuilder for GET /offender/{crn}/documents/{documentId}
    ChainBuilder getOffenderDocument =
            exec(
                    http("GET Offender Document By ID")
                            .get("/offender/#{crn}/documents/#{documentId}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Checks
                            .check(status().is(200))
                            .check(responseTimeInMillis().lte(3000))
            );

    // ChainBuilder for GET /offender/{crn}/detail
    ChainBuilder getOffenderDetail =
            exec(
                    http("GET Offender Detail By CRN")
                            .get("/offender/#{crn}/detail")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists())
                            .check(responseTimeInMillis().lte(2000))
            );

    // ChainBuilder for GET /offender/{crn}/convictions/{convictionId}
    ChainBuilder getOffenderConviction =
            exec(
                    http("GET Offender Conviction By ID")
                            .get("/offender/#{crn}/convictions/#{convictionId}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Checks
                            .check(status().is(200))
                            .check(jsonPath("$.convictionId").is("#{convictionId}"))
                            .check(jsonPath("$").exists())
                            .check(responseTimeInMillis().lte(2500))
            );

    // ChainBuilder for GET /offender/{crn}/convictions/{convictionId}/sentence
    ChainBuilder getOffenderSentence =
            exec(
                    http("GET Offender Sentence Detail")
                            .get("/offender/#{crn}/convictions/#{convictionId}/sentence")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Basic checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists())
                            .check(jsonPath("$.convictionId").is("#{convictionId}"))
                            .check(jsonPath("$.crn").is("#{crn}"))
                            .check(responseTimeInMillis().lte(3000))
            );

    // ChainBuilder for GET /offender/{crn}/convictions/{convictionId}/sentence/custody
    ChainBuilder getOffenderSentenceCustody =
            exec(
                    http("GET Offender Sentence Custody")
                            .get("/offender/#{crn}/convictions/#{convictionId}/sentence/custody")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Basic checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists())
                            .check(jsonPath("$.convictionId").is("#{convictionId}"))
                            .check(jsonPath("$.crn").is("#{crn}"))
                            .check(responseTimeInMillis().lte(3000))
            );

    // ChainBuilder for GET /offender/{crn}/convictions/{convictionId}/breaches/{breachId}
    ChainBuilder getOffenderBreach =
            exec(
                    http("GET Offender Breach Detail")
                            .get("/offender/#{crn}/convictions/#{convictionId}/breaches/#{breachId}")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists())
                            .check(jsonPath("$.breachId").is("#{breachId}"))
                            .check(jsonPath("$.convictionId").is("#{convictionId}"))
                            .check(jsonPath("$.crn").is("#{crn}"))
                            .check(responseTimeInMillis().lte(3000))
            );

    // ChainBuilder for GET custody data
    ChainBuilder getOffenderCustody =
            exec(
                    http("GET Offender Sentence Custody")
                            .get("/offender/#{crn}/convictions/#{convictionId}/sentence/custody")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            .check(status().is(200))
                            .check(jsonPath("$").exists()) // Ensure JSON response is returned
                            .check(jsonPath("$.convictionId").is("#{convictionId}"))
                            .check(jsonPath("$.crn").is("#{crn}"))
                            .check(responseTimeInMillis().lte(3000)) // Optional threshold
            );


    ScenarioBuilder scenario = scenario("PACFS UI - Case Summary Page")
            .feed(courtFeeder)
            .feed(hearingIdFeeder)
            .feed(defendantIdFeeder)
            .feed(noteContentFeeder)
            .feed(commentContentFeeder)
            .feed(outcomeTypeFeeder)
            .feed(crmFeeder)
            .feed(convictionIdTypeFeeder)
            .feed(breachIdTypeFeeder)
            .exec(authenticate)

            .pause(pause)
            .exec(viewCaseSummaryPage)
            .pause(pause)
            .exec(addHearingNote)
            .pause(pause)
            .exec(editHearingNote)
            .pause(pause)
            .exec(deleteHearingNote)
            .pause(pause)
            .exec(addComment)
            .pause(pause)
            .exec(editComment)
            .pause(pause)
            .exec(deleteComment)
            .pause(pause)
            .exec(assignCaseToMyself)
            .pause(pause)
            .exec(setCaseResult)
            .pause(pause)
            .exec(moveCaseToResulted)


            .pause(pause)
            .exec(getOffenderRegistrations)
            .pause(pause)
            .exec(getOffenderProbationStatusDetail)
            .pause(pause)
            .exec(getOffenderProbationRecord)
            .pause(pause)
            .exec(getOffenderDocument)
            .pause(pause)
            .exec(getOffenderDetail)
            .pause(pause)
            .exec(getOffenderConviction)
            .pause(pause)
            .exec(getOffenderSentence)
            .pause(pause)
            .exec(getOffenderSentenceCustody)
            .pause(pause)
            .exec(getOffenderBreach)
            .pause(pause)
            .exec(getOffenderCustody);

    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol);
    }
}
