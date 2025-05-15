/*
 * Copyright 2025 HM Revenue & Customs
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

import connectors.error.{ApiError, SingleErrorBody}
import connectors.response.SessionDataHttpResponse.SessionDataResponse
import models.session.UserSessionData
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationTest


class SessionDataConnectorISpec extends IntegrationTest {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Session-ID" -> sessionId)

  lazy val connector: SessionDataConnector = app.injector.instanceOf[SessionDataConnector]

  val stubGetUrl = s"/income-tax-session-data"
  val sessionDataResponse: UserSessionData = UserSessionData(mtditid = mtditid, nino = nino, sessionId = sessionId)

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.income-tax-session-data.port" -> wireMockPort)
      .build()


  "calling .getSessionData()" should {

    "return session data" when {

      "downstream response is successful and includes valid JSON" in {
        stubGet(stubGetUrl, OK, Json.toJson(sessionDataResponse).toString())

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result shouldBe Right(Some(sessionDataResponse))
      }
    }

    "return None" when {

      "downstream returns NOT_FOUND" in {
        stubGet(stubGetUrl, NOT_FOUND, "")

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result shouldBe Right(None)
      }

      "downstream returns NO_CONTENT" in {
        stubGet(stubGetUrl, NO_CONTENT, "")

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result shouldBe Right(None)
      }
    }

    "return Left(error)" when {

      "downstream returns OK but with an invalid response payload" in {
        stubGet(stubGetUrl, OK, "{}")

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
      }

      "downstream fails with Internal Error" in {
        val serviceUnavailableError = SingleErrorBody("INTERNAL_SERVER_ERROR", s"Failed to retrieve session with id: $sessionId")
        stubGet(stubGetUrl, INTERNAL_SERVER_ERROR, Json.toJson(serviceUnavailableError).toString())

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", s"Failed to retrieve session with id: $sessionId")))
      }

      "downstream returns SERVICE_UNAVAILABLE" in {
        val serviceUnavailableError = SingleErrorBody("SERVICE_UNAVAILABLE", "Internal Server error")
        stubGet(stubGetUrl, SERVICE_UNAVAILABLE, Json.toJson(serviceUnavailableError).toString())

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result shouldBe Left(ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("SERVICE_UNAVAILABLE", "Internal Server error")))
      }

      "downstream fails with unknown error" in {
        val someRandomError = SingleErrorBody("TOO_MANY_REQUESTS", s"some random error")
        stubGet(stubGetUrl, TOO_MANY_REQUESTS, Json.toJson(someRandomError).toString())

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("TOO_MANY_REQUESTS", s"some random error")))
      }

      "downstream fails when there is unexpected response format" in {
        stubGet(stubGetUrl, TOO_MANY_REQUESTS, Json.toJson("unexpected response").toString())

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
      }
    }
  }
}
