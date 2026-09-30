package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder; // 1. Import the FeederBuilder class
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.csv; // 2. Import the 'csv' function
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CaseListSimulation extends Simulation implements PacSimulation {

//  Option 1
//    private static ChainBuilder courtList = exec(
//            http("Load Case List Page")
//                    .get("/select-court/"+courtCode)
//                    .check(css("title").is("Case list - Prepare a case for sentence")));
//==========================================


    // 3. Define the feeder to read from your data file.
    //    '.random( )' tells each user to pick a random court from the list.
    private static final FeederBuilder<String> courtFeeder = csv("data/courts").random();

    // 4. IMPORTANT: We can no longer use the 'courtList' and 'selectCourt' actions
    //    from PacSimulation because they use the hardcoded 'courtCode' variable.
    //    We must redefine them here to use the value from our feeder.

    private static final ChainBuilder selectCourtFromFeeder = exec(
            http("Select Court from Feeder" )
                    .post("/my-courts/setup")
                    .header("Authorization", "Bearer #{accessToken}")

                    // Use '#{CourtCode}' to get the value from the feeder file.
                    // The name 'CourtCode' must match the header in your courts data file.
                    .formParam("court", "#{CourtCode}")
                    .check(css(".govuk-heading-l").is("Which courts do you work in?"))
    );

    private static final ChainBuilder saveCourt =
            exec(
                    http("Save Court")
                            .get("/my-courts/setup?save=true")
                            .header("Authorization", "Bearer #{accessToken}")
                            .check(status().in(200, 302))
            );

    private static final ChainBuilder loadCaseListForSelectedCourt = exec(
            http("Load Case List Page for Selected Court" )
                    // The URL also needs to use the dynamic value from the feeder.
                    .get("/select-court/#{CourtCode}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("title").is("Case list - Prepare a case for sentence")));
    //Option 1
    //ScenarioBuilder scenario = scenario("Case List")

    // 5. Update the scenario to use the new feeder-based actions.
    ScenarioBuilder scenario =
            scenario("PAC – Core User Journey ie. Case List from Feeder")
                    .exec(authenticate)
                    .pause(pause)

            // The .feed() action injects a random row from the feeder into the user's session.
            .feed(courtFeeder)

//            .exec(homePage)
//            .pause(pause)

            //.exec(selectCourt)
            // Replace the original 'selectCourt' with our new feeder-aware version.
            .exec(selectCourtFromFeeder)
            .pause(pause)

            // The original 'saveCourt' from PacSimulation is fine to reuse as it doesn't need the court code.
            .exec(saveCourt)
            .pause(pause)
            //.exec(courtList);

            // Replace the original 'courtList' with our new feeder-aware version.
            .exec(loadCaseListForSelectedCourt);
    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol);
    }
}
