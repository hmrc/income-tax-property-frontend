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
import models.TotalIncome.{Over, Under}
import models.{BalancingCharge, User, UserAnswers}
import models.backend.{BusinessDetails, HttpParserError, PropertyDetails}
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.adjustments.BalancingChargePage
import pages.{IncomeFromPropertyRentalsPage, OtherIncomeFromPropertyPage, TotalIncomePage}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessServiceSpec extends AnyWordSpec
  with FutureAwaits with DefaultAwaitTimeout
  with Matchers {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  val mockBusinessConnector: BusinessConnector = mock[BusinessConnector]

  private val underTest = new BusinessService(mockBusinessConnector)

  "getBusinessDetails" should {
    val user = User("mtditid", "nino", "group", true)

    "return error when fails to get data" in {
      when(mockBusinessConnector.getBusinessDetails(user)) thenReturn Future(Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.getBusinessDetails(user)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return data" in {
      val businessDetails = BusinessDetails(List(PropertyDetails(Some("property"), Some(LocalDate.now), cashOrAccruals = Some(false))))

      when(mockBusinessConnector.getBusinessDetails(user)) thenReturn Future(Right(businessDetails))

      await(underTest.getBusinessDetails(user)) shouldBe Right(businessDetails)
    }
  }

  "Total Income" should {
    "return sum of all income section" in {
      val userAnswers = UserAnswers("test").set(IncomeFromPropertyRentalsPage, BigDecimal(80000)).get
      val totalIncome = underTest.totalIncome(userAnswers)
      totalIncome shouldEqual 80000
    }
  }

  "Maximum Property Income Allowance Combined" should {
    "return sum of all income section and balancing charge" in {
      val userAnswers = UserAnswers("test")
        .set(IncomeFromPropertyRentalsPage, BigDecimal(80000))
        .flatMap(_.set(BalancingChargePage, BalancingCharge(balancingChargeYesNo = true, Some(BigDecimal(10000)))))
        .get

      val totalIncome = underTest.maxPropertyIncomeAllowanceCombined(userAnswers)
      totalIncome shouldEqual 90000
    }
  }

  "Total income" should {
    "under 85k if user selected total income is Under" in {
      val userAnswers = UserAnswers("test").set(TotalIncomePage, Under).get
      val isTotalIncomeUnder85K = underTest.isTotalIncomeUnder85K(userAnswers)
      isTotalIncomeUnder85K shouldBe true
    }
    "over 85k if user selected total income is Over" in {
      val userAnswers = UserAnswers("test").set(TotalIncomePage, Over).get
      val isTotalIncomeUnder85K = underTest.isTotalIncomeUnder85K(userAnswers)
      isTotalIncomeUnder85K shouldBe false
    }
    "under 85k if sum of all income section" in {
      val userAnswers = UserAnswers("test").set(TotalIncomePage, Over)
        .flatMap(_.set(IncomeFromPropertyRentalsPage, BigDecimal(80000))).get
      val isTotalIncomeUnder85K = underTest.isTotalIncomeUnder85K(userAnswers)
      isTotalIncomeUnder85K shouldBe true
    }
  }

}
