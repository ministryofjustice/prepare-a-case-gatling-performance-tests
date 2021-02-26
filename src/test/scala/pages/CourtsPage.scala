//package pages
//
//import config.Config.{headers_1, headers_4, headers_7, headers_8}
//import config.Data.{courts, users}
//import io.gatling.core.Predef.{feed, _}
//import io.gatling.http.Predef.{flushCookieJar, http, status}
//
//object CourtsPage {
//
//  val baseUrl = "https://prepare-a-case-preprod.apps.live-1.cloud-platform.service.justice.gov.uk"
//  val authUrl = "https://sign-in-preprod.hmpps.service.justice.gov.uk/auth"
//
//  val Courts = feed(users)
//    .exec(flushCookieJar)
//    .exec(session => {
//      //println("Logging in as " + session("ParamUsername").as[String])
//      session
//    })
//    .feed(courts,1)
//  .exec(http("SelectCourt")
//    .get(baseUrl + "/select-court/${CourtCode}")
//    .headers(headers_7)
//    .resources(http("GovPNG")
//      .get(baseUrl + "/assets/images/govuk-crest.png")
//      .headers(headers_8)))
//    //.check(status.is(200)))
//    .pause(7)
//    .exec(http("SelectCasesDate")
//      .get(baseUrl + "/${CourtCode}/cases/2021-02-08")
//      .headers(headers_7)
//      .formParam("probationStatus", "Current")
//      .resources(http("GovPNG")
//        .get(baseUrl + "/assets/images/govuk-crest.png")
//        .headers(headers_1),
//        http("IconTagRemovePNG")
//          .get(baseUrl + "/assets/images/icon-tag-remove-cross.svg")))
//    .pause(5)
//    .exec(http("ClickCase")
//      .get(baseUrl + "/${CourtCode}/case/4732032599587/summary")
//
//      .headers(headers_4)
//      .resources(http("GovCrest")
//        .get(baseUrl + "/assets/images/govuk-crest.png")
//        .headers(headers_1))
//    //.check(css("title").is("Case summary - Prepare a case for sentence")))
//      .check(status.is(200)))
//    .pause(6)
//    .exec(http("ClickProbationRecordScreen")
//      .get(baseUrl + "/${CourtCode}/case/4732032599587/record")
//      .headers(headers_4)
//      //.check(css("title").is("Probation record - Prepare a case for sentence"))
//      .resources(http("GovPNG")
//        .get(baseUrl + "/assets/images/govuk-crest.png")
//        .headers(headers_1)))
//
//}
