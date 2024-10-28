package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class CaseListFilteringSimulation extends Simulation implements PacSimulation {

    private static ChainBuilder courtList = exec(
            http("Load Case List Page")
                    .get("/select-court/"+courtCode)
                    .check(css("title").is("Case list - Prepare a case for sentence")));

    private static ChainBuilder caseListFiltering = exec(
        http("Select filters on case list screen")
                .post("/"+courtCode+"/cases")
                .formParam("probationStatus", "CURRENT")
                .formParam("source", "COMMON_PLATFORM")
                .check(css("#pac-filters-applied-probationStatus").exists())
                .check(css("#pac-filters-applied-courtRoom").exists())
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
            .exec(caseListFiltering);
    {
        setUp(scenario.injectOpen(rampUsers(users).during(ramp))).protocols(httpProtocol);
    }
}
