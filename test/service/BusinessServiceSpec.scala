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

import connectors.BusinessConnector
import connectors.error.{ApiError, SingleErrorBody}
import models.IncomeSourcePropertyType.UKProperty
import models.User
import models.backend.{BusinessDetails, HttpParserError, PropertyDetails}
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

class BusinessServiceSpec extends AnyWordSpec with FutureAwaits with DefaultAwaitTimeout with Matchers {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  val mockBusinessConnector: BusinessConnector = mock[BusinessConnector]

  private val underTest = new BusinessService(mockBusinessConnector)

  "getBusinessDetails" should {
    val user = User("mtditid", "nino", "group", Some("agentReferenceNumber"))

    "return error when fails to get data" in {
      when(mockBusinessConnector.getBusinessDetails(user.nino, user.mtditid)) thenReturn Future(
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      )

      await(underTest.getBusinessDetails(user)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return data" in {
      val businessDetails = BusinessDetails(
        List(PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId"))
      )

      when(mockBusinessConnector.getBusinessDetails(user.nino, user.mtditid)) thenReturn Future(Right(businessDetails))

      await(underTest.getBusinessDetails(user)) shouldBe Right(businessDetails)
    }
  }

  "getUkPropertyDetails" should {
    val user = User("mtditid", "nino", "group", Some("agentReferenceNumber"))

    "return error when fails to get data" in {
      when(mockBusinessConnector.getBusinessDetails(user.nino, user.mtditid)) thenReturn Future(
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      )

      await(underTest.getUkPropertyDetails(user.nino, user.mtditid)) shouldBe Left(
        ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)
      )
    }

    "return data" in {
      val details =
        PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

      val businessDetails = BusinessDetails(List(details))

      when(mockBusinessConnector.getBusinessDetails(user.nino, user.mtditid)) thenReturn Future(Right(businessDetails))

      await(underTest.getUkPropertyDetails(user.nino, user.mtditid)) shouldBe Right(Some(details))
    }
  }

}
