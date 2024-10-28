package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class OutcomeListSimulation extends Simulation implements PacSimulation {

    private static ChainBuilder courtList = exec(
            http("Load Case List Page")
                    .get("/select-court/"+courtCode)
                    .check(css("title").is("Case list - Prepare a case for sentence")));

    private static ChainBuilder outcomesList = exec(
            http("Load Outcomes List Page")
                    .get("/"+courtCode+"/outcomes")
                    .check(css("title").is("Hearing outcomes - Prepare a case for sentence")));

    ScenarioBuilder scenario = scenario("Outcomes List")
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
            .exec(outcomesList);
    {
        setUp(scenario.injectOpen(rampUsers(users).during(ramp))).protocols(httpProtocol);
    }
}
