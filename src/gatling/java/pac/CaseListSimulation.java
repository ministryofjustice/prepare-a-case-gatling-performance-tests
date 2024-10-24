package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class CaseListSimulation extends Simulation implements PacSimulation {

    private static ChainBuilder courtList = exec(
            http("Load Case List Page")
                    .get("/select-court/"+courtCode)
                    .check(css("title").is("Case list - Prepare a case for sentence")));

    ScenarioBuilder scenario = scenario("Case List")
            .exec(homePage)
            .pause(pause)
            .exec(authenticate)
            .pause(pause)
            .exec(selectCourt)
            .pause(pause)
            .exec(saveCourt)
            .pause(pause)
            .exec(courtList);
    {
        setUp(scenario.injectOpen(rampUsers(users).during(ramp))).protocols(httpProtocol);
    }
}
