package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.headerRegex;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CaseListSimulation extends Simulation {

    static int nbusers = Integer.getInteger("users", 1);
    static String username = System.getProperty("username");
    static String password = System.getProperty("password");
    static String env = System.getProperty("env", "dev");

    public static final String BASE_URL = "https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk";
    public static final String AUTH_URL = "https://sign-in-"+env+".hmpps.service.justice.gov.uk/auth";

    HttpProtocolBuilder httpProtocol =
            http.baseUrl(BASE_URL)
                    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .acceptEncodingHeader("gzip, deflate, br, zstd")
                    .acceptLanguageHeader("en-GB,en;q=0.9")
                    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0");

    private static ChainBuilder homePage = exec(
            http("HomePage")
                    .get(AUTH_URL + "/sign-in?redirect_uri=https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk/login/callback")
                    .check(headerRegex("Set-Cookie", "XSRF-TOKEN=([^;]+)").saveAs("csrf")));

    private static ChainBuilder authenticate = exec(
            http("Authenticate")
                    .post(AUTH_URL + "/sign-in")
                    .formParam("_csrf", "#{csrf}")
                    .formParam("redirect_uri", "https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk/login/callback")
                    .formParam("username", username)
                    .formParam("password", password)
                    .check(status().is(200))
                    .check(css("title").is("Which courts do you work in? - Prepare a case for sentence")));

    private static ChainBuilder selectCourt = exec(
            http("SelectCourt")
                    .post("/my-courts/setup")
                    .formParam("court", "B14LO")
                    .check(css(".govuk-heading-l").is("Which courts do you work in?")));

    private static ChainBuilder saveCourt = exec(
            http("SaveCourt")
                    .get("/my-courts/setup?save=true"));

    private static ChainBuilder courtList = exec(
            http("Load Court List Page")
                    .get("/select-court/B14LO")
                    .check(css("title").is("Case list - Prepare a case for sentence")));

    ScenarioBuilder users = scenario("Login")
            .exec(homePage)
            .pause(2)
            .exec(authenticate)
            .pause(2)
            .exec(selectCourt)
            .pause(2)
            .exec(saveCourt)
            .pause(2)
            .exec(courtList);
    {
        setUp(
                users.injectOpen(atOnceUsers(nbusers))
        ).protocols(httpProtocol);
    }
}
