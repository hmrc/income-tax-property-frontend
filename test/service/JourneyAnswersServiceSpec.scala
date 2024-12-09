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
import models.JourneyPath.RentARoomExpenses
import models.backend.{HttpParserError, PropertyDetails}
import models.{JourneyContext, User}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyAnswersServiceSpec extends AnyWordSpec with FutureAwaits with DefaultAwaitTimeout with Matchers {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  val mockJourneyAnswersConnector: JourneyAnswersConnector = mock[JourneyAnswersConnector]
  val mockBusinessService: BusinessService = mock[BusinessService]
  val taxYear = 2024
  private val underTest = new JourneyAnswersService(mockJourneyAnswersConnector, mockBusinessService)

  "setStatus" should {
    val user = User("mtditid", "nino", "group", agentRef = Some("agentReferenceNumber"))

    when(mockBusinessService.getUkPropertyDetails(user.nino, user.mtditid)) thenReturn Future(
      Right(
        Option(
          PropertyDetails(
            incomeSourceType = Some("incomeSourceId"),
            tradingStartDate = Some(LocalDate.now),
            accrualsOrCash = Some(true),
            incomeSourceId = "incomeSourceId"
          )
        )
      )
    )

    "return error when fails to get data" ignore {
      when(mockJourneyAnswersConnector.setStatus(any(), any(), any(), any(), any())(any())) thenReturn Future(
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      )

      await(
        underTest.setStatus(
          JourneyContext(
            taxYear = taxYear,
            mtditid = user.mtditid,
            nino = user.nino,
            journeyPath = RentARoomExpenses
          ),
          "completed",
          user
        )
      ) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return data" in {
      when(mockJourneyAnswersConnector.setStatus(any(), any(), any(), any(), any())(any())) thenReturn Future(
        Right("")
      )

      when(mockBusinessService.getUkPropertyDetails(user.nino, user.mtditid)) thenReturn Future(
        Right(
          Option(
            PropertyDetails(
              incomeSourceType = Some("incomeSourceId"),
              tradingStartDate = Some(LocalDate.now),
              accrualsOrCash = Some(true),
              incomeSourceId = "incomeSourceId"
            )
          )
        )
      )

      await(
        underTest.setStatus(
          JourneyContext(
            taxYear = taxYear,
            mtditid = user.mtditid,
            nino = user.nino,
            journeyPath = RentARoomExpenses
          ),
          "inProgress",
          user
        )
      ) shouldBe Right("")

    }
  }

}
