package config

import io.gatling.core.Predef.{configuration, csv}

object Data {

  //data
  val courts = csv("src/test/resources/data/courts").random

  //mined-data
  val users = csv("src/test/resources/data/mined-data/users").random

}
