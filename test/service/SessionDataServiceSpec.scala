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

package service

import config.MockAppConfig
import connectors.error.{ApiError, SingleErrorBody}
import models.errors.MissingAgentClientDetails
import models.session.UserSessionData
import play.api.http.Status.IM_A_TEAPOT
import play.api.mvc.Request
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import mocks.MockSessionDataConnector
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.await
import testHelpers.UserHelper.aUser
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class SessionDataServiceSpec extends AnyWordSpec with MockAppConfig with MockSessionDataConnector with DefaultAwaitTimeout with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val sessionData: UserSessionData = UserSessionData(aUser.sessionId, aUser.mtditid, aUser.nino)

  val testService: SessionDataService = new SessionDataService(
    sessionDataConnector = mockSessionDataConnector,
    config = mockAppConfig
  )

  val dummyError: ApiError = ApiError(IM_A_TEAPOT, SingleErrorBody("", ""))

  ".getSessionData()" when {

    "V&C Session Data service feature is enabled" when {

      "the call to retrieve session data fails" when {

        "the retrieval of fallback data from the session cookie also fails to find the clients details" should {

          "return an error when fallback returns no data" in {
            MockAppConfig.mockSessionServiceEnabled(true)
            mockGetSessionDataFromSessionStore(Left(dummyError))

            implicit val request: Request[_] = FakeRequest()

            val result: MissingAgentClientDetails = intercept[MissingAgentClientDetails](await(testService.getSessionData(aUser.sessionId)(request, hc)))
            result.message shouldBe "Session Data service and Session Cookie both returned empty data"
          }
        }

        "the fallback is successful and retrieves client MTDITID and NINO from the Session Cookie" should {

          "return session data" in {
            MockAppConfig.mockSessionServiceEnabled(true)
            mockGetSessionDataFromSessionStore(Left(dummyError))

            implicit val request: Request[_] = FakeRequest()
              .withSession(
                ("ClientNino", "AA111111A"),
                ("ClientMTDID", "12345678")
              )

            val result: UserSessionData = await(testService.getSessionData(aUser.sessionId)(request, hc))
            result shouldBe UserSessionData(sessionId = aUser.sessionId, mtditid = "12345678", nino = "AA111111A")
          }
        }
      }

      "the call to retrieve session data from the downstream V&C service is successful" should {

        "return the session data" in {
          MockAppConfig.mockSessionServiceEnabled(true)
          mockGetSessionDataFromSessionStore(Right(Some(sessionData)))

          implicit val request: Request[_] = FakeRequest()

          val result: UserSessionData = await(testService.getSessionData(aUser.sessionId)(request, hc))
          result shouldBe sessionData
        }
      }
    }

    "V&C Session Data service feature is DISABLED" when {

      "the retrieval of fallback data from the session cookie also fails to find the clients details" should {

        "return an error when fallback returns no data" in {
          MockAppConfig.mockSessionServiceEnabled(false)

          implicit val request: Request[_] = FakeRequest()

          val result: MissingAgentClientDetails = intercept[MissingAgentClientDetails](await(testService.getSessionData(aUser.sessionId)(request, hc)))
          result.message shouldBe "Session Data service and Session Cookie both returned empty data"
        }
      }

      "the fallback is successful and retrieves client MTDITID and NINO from the Session Cookie" should {

        "return session data" in {
          MockAppConfig.mockSessionServiceEnabled(false)

          implicit val request: Request[_] = FakeRequest()
            .withSession(
              ("ClientNino", "AA111111A"),
              ("ClientMTDID", "12345678")
            )

          val result: UserSessionData = await(testService.getSessionData(aUser.sessionId)(request, hc))
          result shouldBe UserSessionData(sessionId = aUser.sessionId, mtditid = "12345678", nino = "AA111111A")
        }
      }
    }
  }
}
