package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.headerRegex;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public interface PacSimulation {
    int users = Integer.getInteger("users", 2);
    long ramp = Long.getLong("ramp", 10);
    int pause = Integer.getInteger("pause", 2);

    String env = System.getProperty("env", "dev");

    // OAuth client credentials
    String username = System.getProperty("username");
    String password = System.getProperty("password");

    //String courtCode = System.getProperty("courtcode", "B14LO");
    String courtCode = System.getProperty("courtcode", "#{CourtCode}");

    String BASE_URL = "https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk";
    String AUTH_URL = "https://sign-in-"+env+".hmpps.service.justice.gov.uk/auth";

    HttpProtocolBuilder httpProtocol =
            http.baseUrl(BASE_URL)
                   // .acceptHeader("application/json, text/html")
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/x-www-form-urlencoded")
                    .userAgentHeader("Gatling Performance Test");

//    HttpProtocolBuilder httpProtocol =
//            http.baseUrl(BASE_URL)
//                    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
//                    .acceptEncodingHeader("gzip, deflate, br, zstd")
//                    .acceptLanguageHeader("en-GB,en;q=0.9")
//                    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0");

    /**
     * OAuth token request (client_credentials)
     */
    ChainBuilder authenticate = exec(
            http("OAuth Token Request")
                    .post(AUTH_URL + "/oauth/token")
                    .basicAuth(username, password)
                    .formParam("grant_type", "client_credentials")
                    .check(status().is(200))
                    .check(jsonPath("$.access_token").saveAs("accessToken"))
                    .check(jsonPath("$.token_type").is("Bearer"))
    );


    ChainBuilder homePage = exec(
            http("HomePage")
                    .get(AUTH_URL + "/sign-in?redirect_uri=https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk/login/callback")
                    .check(headerRegex("Set-Cookie", "XSRF-TOKEN=([^;]+)").saveAs("csrf")));


   ChainBuilder selectCourt = exec(
            http("SelectCourt")
                    .post("/my-courts/setup")
                    .formParam("court", courtCode)
                    .check(css(".govuk-heading-l").is("Which courts do you work in?")));

//   ChainBuilder saveCourt = exec(
//            http("SaveCourt")
//                    .get("/my-courts/setup?save=true"));
}
