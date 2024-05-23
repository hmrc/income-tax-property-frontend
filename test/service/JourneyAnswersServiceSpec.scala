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

package service

import connectors.JourneyAnswersConnector
import connectors.error.{ApiError, SingleErrorBody}
import models.backend.HttpParserError
import models.{FetchedBackendData, User}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyAnswersServiceSpec extends AnyWordSpec with FutureAwaits with DefaultAwaitTimeout with Matchers {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  val mockJourneyAnswersConnector: JourneyAnswersConnector = mock[JourneyAnswersConnector]

  private val underTest = new JourneyAnswersService(mockJourneyAnswersConnector)

  "setStatus" should {
    val user = User("mtditid", "nino", "group", isAgent = true, agentRef = Some("agentReferenceNumber"))

    "return error when fails to get data" ignore {
      when(mockJourneyAnswersConnector.setStatus(any(), any(), any(),any(),any())(any())) thenReturn Future(
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      )

      await(underTest.setStatus(2024, "", "", user)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return data" in {
      val fetchedBackendData = FetchedBackendData(None, None, None, None, None, None, None)
      when(mockJourneyAnswersConnector.setStatus(any(), any(), any(), any(), any())(any())) thenReturn Future(
        Right(fetchedBackendData)
      )

      await(underTest.setStatus(2024, "", "", user)) shouldBe Right(fetchedBackendData)

    }
  }


}
