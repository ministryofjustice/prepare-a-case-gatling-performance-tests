import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import config.Config.{headers_0, headers_2}
import config.Data.courts
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class ProbationRecordScreen extends Simulation {

  //Create an output to help monitoring of devs.
  val createOutputVariables = {
    val fos = new java.io.FileOutputStream("createOutputVariables.csv")
    new java.io.PrintWriter(fos, true)
  }
  createOutputVariables.print("caseNo,timeStamp\n")

  private def getProperty(propertyName: String, defaultValue: String): String = {
    {
      {
        Option(System.getenv(propertyName))
          .orElse(Option(System.getProperty(propertyName)))
          .getOrElse(defaultValue)
      }
    }

    //Date for message.
  }

  //For generating date with time.
  def local_nowTime():String = {

    LocalDateTime.now.format(DateTimeFormatter.ofPattern("YYYYMMdd_HHmmss"))
  }

  //Set user as (message count) in this case set to 1.
  def userCount: Int = getProperty("Users", "2").toInt

  def dateOfTest: String = getProperty("Date", "2021-03-30")

  //def rampDuration: Int = getProperty("Ramp_Duration", "10").toInt
  def env: String = getProperty("Env","preprod")

  //User default to using Haseeb.khan user account
  def username: String = getProperty("username", "Haseeb.khan")

  //User password required.
  def passwd: String = getProperty("password", "LoadTesting")

  //Run once through set duration to 0.
  def testDuration: Int = getProperty("Duration", "0").toInt

  def thinkTime: Int = getProperty("ThinkTime", "0").toInt

  before {
    println(s"Running tests with ${userCount} users.")
    println(s"Test duration is: ${testDuration} minutes.")
    println(s"The enviroment that is being tested against is: ${env}.")
    println(s"The test is running with pauses/thinktimes of ${thinkTime} seconds between requests.")
    println(s"The test will run against the following date: ${dateOfTest}")
    println(s"The following account is used in this test: ${username}")
  }

  val httpProtocol = http
    .baseUrl("https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:85.0) Gecko/20100101 Firefox/85.0")

  val baseUrl = "https://prepare-a-case-"+env+".apps.live-1.cloud-platform.service.justice.gov.uk"
  val authUrl = "https://sign-in-"+env+".hmpps.service.justice.gov.uk/auth"

  val scn = scenario("ProbationRecordScreen")
    .exec(session => session.set("local_nowTime", local_nowTime()))
    .exec(flushCookieJar)

    .exec(session => {
      println("Logging in as: " + username)
      session
    })
    .feed(courts)
    //.exec(Homepage.Login)
    //.exec(CourtsPage.Courts)
    .exec(http("HomePage")
      .get(baseUrl)
      .headers(headers_0)
      .check(status.is(200))
      .check(css("title").saveAs("loginTitle")))
    .pause(thinkTime)
    .exec(http("LoginForm")
      .post(authUrl + "/login")
      .headers(headers_2)
      .formParam("username", username)
      .formParam("password", passwd)
      .check(status.is(200)))
    //.exec(getCookieValue(CookieKey("connect.sid")))
    .pause(thinkTime)
    .exec(http("SelectCourt")
      .get("/select-court/${CourtCode}"))
    .pause(thinkTime)
    .exec(http("GetCasesofDate")
      .get("/${CourtCode}/cases/"+dateOfTest+"")
      .check(css("title").is("Case list - Prepare a case for sentence")))
    .pause(thinkTime)
    .exec(http("PostCurrentStatus")
      .post("/${CourtCode}/cases/"+dateOfTest+"")
      .formParam("probationStatus", "Current")
    .check(regex("/case/([0-9A-Z]*)/summary").findAll.saveAs("caseNo")))
    .exec(session => {
      val i = session("caseNo").as[List[String]].length
      val start = 1
      val test = session("caseNo").as[List[String]]
      val end   = i
      val num = new scala.util.Random
      val rnd = start + num.nextInt( (end - start))
      session.set("rndCaseNo", test(rnd))
    })
    .exec(session => {
      println("This is is the newly created variable: " + session("rndCaseNo").as[String])
      session
    })
    .pause(thinkTime)
    .exec(http("GetSummary")
      .get("/${CourtCode}/case/${rndCaseNo}/summary")
      .check(css("title").is("Case summary - Prepare a case for sentence"))
      .check(status.is(200)))
    .pause(thinkTime)
    .exec(http("GetRecordScreen")
      .get("/${CourtCode}/case/${rndCaseNo}/record")
      .requestTimeout(1.minutes)
      .check(css("title").is("Probation record - Prepare a case for sentence"))
      .check(status.is(200))
    .check(css("#main-content > div > div.govuk-grid-column-two-thirds > h2").exists)
      .check(css("body").saveAs("body"))
    .check(responseTimeInMillis))

  .exec(session => {
      for (i <- 1 until 1000) {
        createOutputVariables.println(session("rndCaseNo").as[String],session("local_nowTime").as[String])
      }
        session
    })

//  createOutputVariables.print("caseNo\n")
//  for (i <- 1 until 2) {
//    createOutputVariables.println("${rndCaseNo}")
//  }

//    foreach("${rndCaseNo}", "rndCaseNo")
//    {
//      exec(session => {
//        {
//          createOutputVariables.println(session("rndCaseNo").as[String])
//        }
//        session
//      })
//    }




//    .exec(session => {
//      println(session("body").as[String])
//      session
//    })
//  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
//}

  setUp(

    scn.inject
    (nothingFor(5 seconds),
      //atOnceUsers(userCount)
      rampUsers(userCount).during(testDuration.minutes)
    )

  )
    .protocols(httpProtocol)

}