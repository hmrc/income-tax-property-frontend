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

import connectors.SessionDataConnector
import connectors.response.SessionDataHttpResponse.SessionDataResponse
import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockSessionDataConnector extends MockFactory { this: TestSuite =>

  val mockSessionDataConnector: SessionDataConnector = mock[SessionDataConnector]

  private type MockType = CallHandler1[HeaderCarrier, Future[SessionDataResponse]]

  private def mockFunction: MockType =
    (mockSessionDataConnector.getSessionData(_: HeaderCarrier)).expects(*)

  def mockGetSessionDataFromSessionStore(resp: SessionDataResponse): MockType#Derived =
    mockFunction.returning(Future.successful(resp))

  def mockGetSessionDataFromSessionStoreException(err: Throwable): MockType#Derived =
    mockFunction.returning(Future.failed(err))
}
