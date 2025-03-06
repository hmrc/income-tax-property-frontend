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

import models.PropertyAbout
import base.SpecBase
import connectors.PropertySubmissionConnector
import connectors.error.{ApiError, SingleErrorBody}
import models.TotalIncome.Under
import models.backend.{HttpParserError, PropertyDetails}
import models._
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import testHelpers.Fixture
import uk.gov.hmrc.http.HeaderCarrier
import models.IncomeSourcePropertyType

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertySubmissionServiceSpec extends SpecBase with FutureAwaits with DefaultAwaitTimeout with Fixture {

  val propertyPeriodicSubmissionConnector: PropertySubmissionConnector = mock[PropertySubmissionConnector]
  val mockBusinessConnector: BusinessService = mock[BusinessService]
  val taxYear = 2024
  val user: User = User("mtditid", "nino", "individual", Some("agentReferenceNumber"))
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val propertyPeriodSubmissionService =
    new PropertySubmissionService(propertyPeriodicSubmissionConnector, mockBusinessConnector)

  "getUKPropertyPeriodicSubmission" - {
    "return success when connector returns success" in {
      val resultFromConnector = FetchedPropertyData(ukPropertyData,foreignPropertyData, ukAndForeignPropertyData)
      val incomeSourceId = "incomeSourceId"
      val details =
        PropertyDetails(Some(IncomeSourcePropertyType.UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), incomeSourceId)

      when(mockBusinessConnector.getUkPropertyDetails(user.nino, user.mtditid)) thenReturn Future(Right(Some(details)))
      when(
        propertyPeriodicSubmissionConnector.getPropertySubmission(taxYear, incomeSourceId, user)
      ) thenReturn Future.successful(Right(resultFromConnector))

      val resultFromService = propertyPeriodSubmissionService.getUKPropertySubmission(taxYear, user)

      whenReady(resultFromService) {
        case Right(r) => r mustBe resultFromConnector
        case Left(_)  => fail("Service should return success when connector returns success")
      }
    }

    "return FetchedPropertyData with empty JsObject when connector returns failure" in {
      val resultFromConnector = ApiError(status = 500, SingleErrorBody("500", "Some error"))
      val incomeSourceId = "incomeSourceId"
      val details =
        PropertyDetails(Some(IncomeSourcePropertyType.UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), incomeSourceId)

      when(mockBusinessConnector.getUkPropertyDetails(user.nino, user.mtditid)) thenReturn Future(Right(Some(details)))

      when(
        propertyPeriodicSubmissionConnector.getPropertySubmission(taxYear, incomeSourceId, user)
      ).thenReturn(Future.successful(Left(resultFromConnector)))

      val resultFromService = propertyPeriodSubmissionService.getUKPropertySubmission(taxYear, user)

      whenReady(resultFromService) {

        case Right(_) =>
          fail("Service should return Left")
        case Left(_) =>
          succeed
      }
    }
  }

  "getForeignPropertyPeriodicSubmission" - {
    "return success when connector returns success" in {
      val resultFromConnector = FetchedPropertyData(ukPropertyData,foreignPropertyData, ukAndForeignPropertyData)
      val foreignIncomeSourceId = "foreignIncomeSourceId"
      val details =
        PropertyDetails(Some(IncomeSourcePropertyType.ForeignProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), foreignIncomeSourceId)

      when(mockBusinessConnector.getForeignPropertyDetails(user.nino, user.mtditid)) thenReturn Future(Right(Some(details)))
      when(
        propertyPeriodicSubmissionConnector.getPropertySubmission(taxYear, foreignIncomeSourceId, user)
      ) thenReturn Future.successful(Right(resultFromConnector))

      val resultFromService = propertyPeriodSubmissionService.getForeignPropertySubmission(taxYear, user)

      whenReady(resultFromService) {
        case Right(r) => r mustBe resultFromConnector
        case Left(_)  => fail("Service should return success when connector returns success")
      }
    }

    "return FetchedPropertyData with empty JsObject when connector returns failure" in {
      val resultFromConnector = ApiError(status = 500, SingleErrorBody("500", "Some error"))
      val foreignIncomeSourceId = "foreignIncomeSourceId"

      val details =
        PropertyDetails(Some(IncomeSourcePropertyType.ForeignProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), foreignIncomeSourceId)

      when(mockBusinessConnector.getForeignPropertyDetails(user.nino, user.mtditid)) thenReturn Future(Right(Some(details)))

      when(
        propertyPeriodicSubmissionConnector.getPropertySubmission(taxYear, foreignIncomeSourceId, user)
      ).thenReturn(Future.successful(Left(resultFromConnector)))

      val resultFromService = propertyPeriodSubmissionService.getForeignPropertySubmission(taxYear, user)

      whenReady(resultFromService) {

        case Right(_) =>
          fail("Service should return Left")
        case Left(_) =>
          succeed
      }
    }
  }

  "saveJourneyAnswers" - {
    val user = User("mtditid", "nino", "group", Some("agentReferenceNumber"))
    val taxYear = 2024
    val context =
      JourneyContext(
        taxYear = taxYear,
        mtditid = user.mtditid,
        nino = user.nino,
        journeyPath = JourneyPath.PropertyAbout
      )
    val propertyAbout = PropertyAbout(Under, ukProperty = Some(UKPropertySelect.values), reportPropertyIncome = Some(true))

    "return error when fails to get property data" in {
      when(mockBusinessConnector.getUkPropertyDetails(user.nino, user.mtditid)) thenReturn Future(
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      )

      await(propertyPeriodSubmissionService.saveJourneyAnswers(context, propertyAbout)) mustBe Left(
        HttpParserError(INTERNAL_SERVER_ERROR)
      )
    }

    "return empty for successful save" in {
      val details =
        PropertyDetails(Some(IncomeSourcePropertyType.UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

      when(mockBusinessConnector.getUkPropertyDetails(user.nino, user.mtditid)) thenReturn Future(Right(Some(details)))

      when(
        propertyPeriodicSubmissionConnector.saveJourneyAnswers[PropertyAbout](context, propertyAbout, "incomeSourceId")
      ) thenReturn Future(Right())

      await(propertyPeriodSubmissionService.saveJourneyAnswers(context, propertyAbout)) mustBe Right()
    }

    "return error for failure save" in {
      val details =
        PropertyDetails(Some(IncomeSourcePropertyType.UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

      when(mockBusinessConnector.getUkPropertyDetails(user.nino, user.mtditid)) thenReturn Future(Right(Some(details)))

      when(
        propertyPeriodicSubmissionConnector.saveJourneyAnswers[PropertyAbout](context, propertyAbout, "incomeSourceId")
      ) thenReturn Future(Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(propertyPeriodSubmissionService.saveJourneyAnswers(context, propertyAbout)) mustBe Left(
        HttpParserError(INTERNAL_SERVER_ERROR)
      )
    }
  }

}
