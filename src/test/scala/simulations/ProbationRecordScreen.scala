
import config.Config._
import config.Data.{courts, users}
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class ProbationRecordScreen extends Simulation {

  val httpProtocol = http
    .baseUrl("https://prepare-a-case-preprod.apps.live-1.cloud-platform.service.justice.gov.uk")
    .inferHtmlResources()
    .acceptHeader("image/webp,*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:85.0) Gecko/20100101 Firefox/85.0")

  val authUrl = "https://sign-in-preprod.hmpps.service.justice.gov.uk/auth"

  val scn = scenario("ProbationRecordScreen")
    .feed(users)
    .exec(flushCookieJar)
    .exec(session => {
      println("Logging in as " + session("ParamUsername").as[String])
      session
    })
    .feed(courts)
    //.exec(Homepage.Login)
    //.exec(CourtsPage.Courts)
    .exec(http("LoginPage")
      .get(authUrl + "/login")
      .headers(headers_0)
      .check(status.is(200)))
    .pause(10)
    .exec(http("LoginForm")
      .post(authUrl + "/login")
      .headers(headers_2)
      .formParam("username", "${ParamUsername}")
      .formParam("password", "${ParamPassword}")
      .check(status.is(200)))
    .pause(10)
    .exec(http("SelectCourt")
      .get("/select-court/${CourtCode}")
      .headers(headers_4))
    .pause(10)
    .exec(http("GetCasesofDate")
      .get("/${CourtCode}/cases/2021-02-08")
      .headers(headers_4)
      .check(css("title").is("Cases - Prepare a case for sentence")))
    .pause(10)
    .exec(http("PostCurrentStatus")
      .post("/${CourtCode}/cases/2021-02-08")
      .headers(headers_8)
      .formParam("probationStatus", "Current")
    .check(regex("/case/([0-9A-Z]*)/summary").saveAs("caseNo")))
    .exec(session => {
      println("CaseNo is: " + session("caseNo").as[String])
      session
    })
    .pause(2)
    .exec(http("GetSummary")
      .get("/${CourtCode}/case/${caseNo}/summary")
      .headers(headers_4)
      .check(css("title").is("Case summary - Prepare a case for sentence"))
      .check(status.is(200)))
    .pause(6)
    .exec(http("GetRecordScreen")
      .get("/${CourtCode}/case/${caseNo}/record")
      .headers(headers_4)
      .check(css("title").is("Probation record - Prepare a case for sentence"))
      .check(status.is(200)))

  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}