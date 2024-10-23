package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.http.HttpDsl.headerRegex;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public interface PacSimulation {
    int users = Integer.getInteger("users", 1);
    String username = System.getProperty("username");
    String password = System.getProperty("password");
    String env = System.getProperty("env", "dev");
    long ramp = Long.getLong("ramp", 10);
    int pause = Integer.getInteger("pause", 2);

    String BASE_URL = "https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk";
    String AUTH_URL = "https://sign-in-"+env+".hmpps.service.justice.gov.uk/auth";

    HttpProtocolBuilder httpProtocol =
            http.baseUrl(BASE_URL)
                    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .acceptEncodingHeader("gzip, deflate, br, zstd")
                    .acceptLanguageHeader("en-GB,en;q=0.9")
                    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0");

   ChainBuilder homePage = exec(
            http("HomePage")
                    .get(AUTH_URL + "/sign-in?redirect_uri=https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk/login/callback")
                    .check(headerRegex("Set-Cookie", "XSRF-TOKEN=([^;]+)").saveAs("csrf")));

   ChainBuilder authenticate = exec(
            http("Authenticate")
                    .post(AUTH_URL + "/sign-in")
                    .formParam("_csrf", "#{csrf}")
                    .formParam("redirect_uri", "https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk/login/callback")
                    .formParam("username", username)
                    .formParam("password", password)
                    .check(status().is(200))
                    .check(css("title").is("Which courts do you work in? - Prepare a case for sentence")));

   ChainBuilder selectCourt = exec(
            http("SelectCourt")
                    .post("/my-courts/setup")
                    .formParam("court", "B14LO")
                    .check(css(".govuk-heading-l").is("Which courts do you work in?")));

   ChainBuilder saveCourt = exec(
            http("SaveCourt")
                    .get("/my-courts/setup?save=true"));
}
