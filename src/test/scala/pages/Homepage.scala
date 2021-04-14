package pages

import config.Config._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

//import util.JsfUtils._

object Homepage {
  //val pageUrl = "/NDelius-war/delius/JSP/homepage.jsp"
  //val authUrl = "/NDelius-war/delius/JSP/auth/j_security_check"
  val baseUrl = "https://prepare-a-case-preprod.apps.live-1.cloud-platform.service.justice.gov.uk"
  val authUrl = "https://sign-in-preprod.hmpps.service.justice.gov.uk/auth"


  val Login = exec(flushCookieJar)
    .exec(session => {
      println("Logging in as " + session("ParamUsername").as[String])
      session
    })
    .exec(http("LoginPage")
      .get(authUrl + "/login")
      .headers(headers_0)
      .check(status.is(200))
      .resources(http("LoginPageHeaders")
        .get(authUrl + "/js/govuk-frontend-3.10.2.min.js")
        .headers(headers_1)))
    .pause(10)
    .exec(http("LoginForm")
      .post(authUrl + "/login")
      .headers(headers_2)
      .formParam("username", "${ParamUsername}")
      .formParam("password", "${ParamPassword}")
      .resources(http("FontLight")
        .get(baseUrl + "/assets/fonts/light-94a07e06a1-v2.woff2")
        .headers(headers_4),
        http("FontBold")
          .get(baseUrl + "/assets/fonts/bold-b542beb274-v2.woff2")
          .headers(headers_4),
        http("AppleTouchIcon")
          .get(baseUrl + "/assets/images/govuk-apple-touch-icon-180x180.png"))
      .check(status.is(200)))

    //.check(css("title").is("Case summary - Prepare a case for sentence"))
    .pause(6)
}