package util

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.css.CssCheckType
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import jodd.lagarto.dom.NodeSelector

object JsfUtils {
  def jsfGet(name: String, url: String): HttpRequestBuilder = http(name).get(url)
    .check(currentLocation.saveAs("currentLocation"))
    .check(jsfViewStateCheck)
  def jsfPost(name: String, url: String): HttpRequestBuilder = http(name).post(url)
    .formParam("javax.faces.ViewState", "${viewState}")
    .check(currentLocation.saveAs("currentLocation"))
    .check(jsfViewStateCheck)
  val jsfViewStateCheck: CheckBuilder[CssCheckType, NodeSelector, String] = css("input[name='javax.faces.ViewState']", "value").optional.saveAs("viewState")
  def groupExec(request: HttpRequestBuilder): ChainBuilder = {
    val name = request.commonAttributes.requestName
    group(name) {
      exec(request)
    }
  }
}
