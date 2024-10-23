package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class OutcomeListFilteringSimulation extends Simulation implements PacSimulation {

    private static ChainBuilder courtList = exec(
            http("Load Case List Page")
                    .get("/select-court/B14LO")
                    .check(css("title").is("Case list - Prepare a case for sentence")));

    private static ChainBuilder outcomesList = exec(
            http("Load Outcomes List Page")
                    .get("/B14LO/outcomes")
                    .check(css("title").is("Hearing outcomes - Prepare a case for sentence")));

    private static ChainBuilder outcomeListFiltering = exec(
        http("Select filters on outcomes list screen")
                .get("/B14LO/outcomes?hearingDate=NONE&outcomeType=REPORT_REQUESTED")
                .check(css("#pac-filters-applied-outcomeType").exists())
    );

    ScenarioBuilder scenario = scenario("Case List")
            .exec(homePage)
            .pause(pause)
            .exec(authenticate)
            .pause(pause)
            .exec(selectCourt)
            .pause(pause)
            .exec(saveCourt)
            .pause(pause)
            .exec(courtList)
            .pause(pause)
            .exec(outcomesList)
            .pause(pause)
            .exec(outcomeListFiltering);
    {
        setUp(scenario.injectOpen(rampUsers(users).during(ramp))).protocols(httpProtocol);
    }
}
