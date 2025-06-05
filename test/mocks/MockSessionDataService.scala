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

package mocks

import models.session.UserSessionData
import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.mvc.Request
import service.SessionDataService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockSessionDataService extends MockFactory { this: TestSuite =>

  val mockSessionDataService: SessionDataService = mock[SessionDataService]

  private type MockType = CallHandler3[String, Request[_], HeaderCarrier, Future[UserSessionData]]

  private def mockFunction(sessionId: String): MockType =
    (mockSessionDataService
      .getSessionData(_: String)(_: Request[_], _: HeaderCarrier))
      .expects(sessionId, *, *)

  def mockGetSessionData(sessionId: String)(resp: UserSessionData): MockType =
    mockFunction(sessionId).returning(Future.successful(resp))

  def mockGetSessionDataException(sessionId: String)(err: Throwable): MockType =
    mockFunction(sessionId).returning(Future.failed(err))

}
