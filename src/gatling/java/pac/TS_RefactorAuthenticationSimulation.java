package pac;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Requirement 1: PACFS UI - Authentication
 * Verifies that a user can login and access the /my-courts endpoint.
 */
public class TS_RefactorAuthenticationSimulation extends Simulation implements PacSimulation {

    ScenarioBuilder scenario = scenario("PACFS UI - Authentication" )
            .exec(authenticate) // Uses the OAuth token request from PacSimulation
            .pause(pause)
            // DEBUG TOKEN
            .exec(session -> {
                String token = session.getString("accessToken");
                System.out.println("Access Token: " + token);
                return session;
            });
    {
        setUp(
                scenario.injectOpen(
                        rampUsers(users).during(ramp)
                )
        ).protocols(httpProtocol );
    }
}
