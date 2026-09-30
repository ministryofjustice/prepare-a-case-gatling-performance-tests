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
 * Requirement 6: PACFS UI - Search page
 * Covers searching for cases by CRN or defendant's name, paging through results, and search validation.
 */
public class TS_RefactorSearchPageSimulation extends Simulation implements PacSimulation {

    private static final FeederBuilder<String> courtFeeder = csv("data/courts.csv").random();
    private static final FeederBuilder<String> prnFeeder = csv("data/courts.csv").random();
    private static final FeederBuilder<String> crmFeeder = csv("data/courts.csv").random();

    // Feeder for search terms (CRN or defendant name )
    private static final List<Map<String, Object>> searchTermsData = Arrays.asList(
            Collections.singletonMap("searchTerm", "CRN123456"), // Example CRN
            Collections.singletonMap("searchTerm", "Smith")      // Example defendant name
    );
    private static final FeederBuilder<Object> searchTermFeeder = listFeeder(searchTermsData).random();

    // Scenario: Searching for a case
    private static final ChainBuilder submitValidSearch = exec(
            http("Submit Valid Search")
                    .get("/case-search?term=#{searchTerm}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Sign in")) // Assuming a heading for search results
    );

    // Scenario: Paging through search results
    private static final ChainBuilder pageThroughSearchResults = exec(
            http("Page Through Search Results - Page 2")
                    .get("/case-search?term=#{searchTerm}&page=2")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("h1").is("Sign in")) // Still on search results page
    );

    // Scenario: Search validation (submitting with no input)
    private static final ChainBuilder submitEmptySearch = exec(
            http("Submit Empty Search")
                    .get("/case-search?term=") // Empty search term
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200)) // Assuming it returns 200 with an error message on the page
            //.check(css(".govuk-error-summary__title").is("There is a problem")) // Example error check
    );


    // Example ChainBuilder for GET /subject-access-request
    ChainBuilder getSubjectAccessRequest =
            exec(
                    http("GET Subject Access Request")
                            .get("/subject-access-request")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Query Parameters
                            .queryParam("prn", "#{prn}") // from CSV or session
                            .queryParam("crn", "#{crn}") // from CSV or session
                            .queryParam("fromDate", "2026-01-01") // optional, adjust as needed
                            .queryParam("toDate", "2026-02-22")   // optional, adjust as needed
                            // Checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists())
                            .check(responseTimeInMillis().lte(2000))
            );

    // Example ChainBuilder for GET /search
    ChainBuilder searchCasesByCrnOrName =
            exec(
                    http("GET Search Cases")
                            .get("/search")
                            .header("Authorization", "Bearer #{accessToken}")
                            .header("Accept", "application/json")
                            // Query parameters: page, size, term, type
                            .queryParam("page", 1)
                            .queryParam("size", 1)
                            .queryParam("term", "#{crn}") // or "#{defendantName}" if searching by name
                            .queryParam("type", "CRN") // could be "NAME" if needed
                            // Checks
                            .check(status().is(200))
                            .check(jsonPath("$").exists())
                            .check(responseTimeInMillis().lte(2000))
            );

    ScenarioBuilder scenario = scenario("PACFS UI - Search Page")
            .feed(courtFeeder)
            .feed(prnFeeder)
            .feed(crmFeeder)
            .pause(pause)
            .exec(authenticate)
            .pause(pause)

            .feed(searchTermFeeder)
            .exec(submitValidSearch)
            .pause(pause)
            .exec(pageThroughSearchResults)
            .pause(pause)
            .exec(submitEmptySearch)

            .pause(pause)
            .exec(getSubjectAccessRequest)
            .pause(pause)
            .exec(searchCasesByCrnOrName);

    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol);
    }
}
