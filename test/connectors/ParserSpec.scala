/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import connectors.error.{ApiError, MultiErrorsBody, SingleErrorBody}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

class ParserSpec extends AnyWordSpec with Matchers {

  private val underTest = new Parser {
    override val parserName: String = "TestParser"
  }

  def httpResponse(json: JsValue =
                   Json.parse(
                     """{"failures":[
                       |{"code":"SERVICE_UNAVAILABLE","reason":"The service is currently unavailable"},
                       |{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}]}""".stripMargin)): HttpResponse = HttpResponse(
    INTERNAL_SERVER_ERROR,
    json,
    Map("CorrelationId" -> Seq("1234645654645"))
  )

  "TestParser" should {
    "return the the correct error" in {
      val result = underTest.badSuccessJsonResponse
      result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
    }

    "handle multiple errors" in {
      val response: HttpResponse = httpResponse()
      val result = underTest.handleError(response, response.status)
      result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, MultiErrorsBody(Seq(
        SingleErrorBody("SERVICE_UNAVAILABLE", "The service is currently unavailable"),
        SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")
      ))))
    }

    "handle single errors" in {
      val response = httpResponse(Json.parse("""{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}""".stripMargin))
      val result = underTest.handleError(response, response.status)
      result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")))
    }

    "handle response that is neither a single error or multiple errors" in {
      val response = httpResponse(Json.obj())
      val result = underTest.handleError(response, response.status)
      result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
    }

    "handle response when the response body is not json" in {
      val response = HttpResponse(INTERNAL_SERVER_ERROR, "", Map("CorrelationId" -> Seq("1234645654645")))
      val result = underTest.handleError(response, response.status)
      result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
    }
  }
}
