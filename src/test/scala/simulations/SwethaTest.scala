package com.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.Console.println

class RecordedSimulation extends Simulation {

  val CsrfPattern= """<input type="hidden" name="_csrf" value="([^"]+)""""
  def saveCsrfToken()=regex(_=>CsrfPattern).saveAs("_csrf")
  val httpProtocol = http
    .baseURL("https://prepare-a-case-preprod.apps.live-1.cloud-platform.service.justice.gov.uk")
    .inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png"""), WhiteList())



  private val scn = scenario("RecordedSimulation").exec(http("getAuthPage").get("/auth/sign-in")
    .check(saveCsrfToken())
    .check(status.is(200))
    )

    .exec(session => {
      println( "CSRF TOKEN:" )

      println(session( "_csrf").as[String] )
       session
     })



    .exec(http("PostAuthPage").post("/auth/sign-in")
      .formParam("""_csrf""", """${_csrf}""")
    .formParam("redirect_uri", "https://prepare-a-case-preprod.apps.live-1.cloud-platform.service.justice.gov.uk/login/callback").formParam("username", "SWETHA.PALREDDY").formParam("password", "RakshanKanna225")
      .check(status.is(200))
      .check(css("title").is("Case list - Prepare a case for sentence")))

    //pause(10)
    .exec(http("summary").get("/B20EB/hearing/b19a5527-dc5a-4454-9252-a66c565f6a32/defendant/d691efd3-d6f4-4726-a5e9-f7d94b306d74/summary")
      .check(css("title").is("Case summary - Prepare a case for sentence")))

    //pause(10)
    .exec(http("addNote").post("/B20EB/hearing/b19a5527-dc5a-4454-9252-a66c565f6a32/defendant/d691efd3-d6f4-4726-a5e9-f7d94b306d74/summary/comments").formParam("note", "testing").formParam("caseId", "99b55a97-6143-4a38-9dd0-1f607e8e2684")
      .check(css("title").is("Case summary - Prepare a case for sentence")))

    .exec(http("addComment").post("/B20EB/hearing/b19a5527-dc5a-4454-9252-a66c565f6a32/defendant/d691efd3-d6f4-4726-a5e9-f7d94b306d74/summary/comments").formParam("comment", "testing").formParam("caseId", "99b55a97-6143-4a38-9dd0-1f607e8e2684")
      .check(css("title").is("Case summary - Prepare a case for sentence")))

    //pause(30)

    //	.exec(http("fileUpload").post("/B20EB/hearing/b19a5527-dc5a-4454-9252-a66c565f6a32/defendant/d691efd3-d6f4-4726-a5e9-f7d94b306d74/summary/files").body(RawFileBody("io/gatling/demo/recordedsimulation/0001_request.json"))
    //		.check(css("title").is("Case summary - Prepare a case for sentence")))




    .exec(http("addHearingOutcome").post("/B20EB/hearing/b19a5527-dc5a-4454-9252-a66c565f6a32/defendant/d691efd3-d6f4-4726-a5e9-f7d94b306d74/summary/add-hearing-outcome").formParam("targetHearingId", "b19a5527-dc5a-4454-9252-a66c565f6a32").formParam("targetDefendantId", "d691efd3-d6f4-4726-a5e9-f7d94b306d74").formParam("hearingOutcomeType", "CROWN_PLUS_PSR")
      .check(css("title").is("Hearing outcomes - Prepare a case for sentence"))
    )


    .exec(http("inProgress").get("/B20EB/outcomes/in-progress?hearingDate=NONE&outcomeType=CROWN_PLUS_PSR")
      .check(css("title").is("Hearing outcomes - Prepare a case for sentence"))
    )

    .exec(http("assignToMe").post("/B20EB/outcomes?page=1").formParam("action", "assign").formParam("defendantHearingId", "7394fdb3-fc6d-4947-96b4-5fa1b5294a2e_64abc2e8-c4a7-4cb7-b1b6-601d91528932")
      .check(css("title").is("Hearing outcomes - Prepare a case for sentence"))
    )


    .exec(http("moveToResulted").get("/B20EB/outcomes/hearing/b19a5527-dc5a-4454-9252-a66c565f6a32/defendant/d691efd3-d6f4-4726-a5e9-f7d94b306d74/move-to-resulted?defendantName=Cyndi%20LAUPER")
      .check(css("title").is("Hearing outcomes - Prepare a case for sentence"))
    )



  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}