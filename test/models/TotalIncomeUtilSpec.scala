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

package models

import base.SpecBase
import models.TotalIncome.{Over, Under}
import models.TotalIncomeUtils.{incomeAndBalancingChargeCombined, isTotalIncomeUnder85K, totalIncome}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.TotalIncomePage
import pages.adjustments.BalancingChargePage
import pages.propertyrentals.income.PropertyRentalIncomePage

class TotalIncomeUtilSpec extends SpecBase {
  "Total Income util" - {
    "return sum of all income section" in {
      val userAnswers = UserAnswers("test").set(PropertyRentalIncomePage(Rentals), BigDecimal(80000)).get
      totalIncome(userAnswers, Rentals) shouldEqual 80000
    }

    "return sum of all income section and balancing charge" in {
      val userAnswers = UserAnswers("test")
        .set(PropertyRentalIncomePage(Rentals), BigDecimal(80000))
        .flatMap(
          _.set(BalancingChargePage(Rentals), BalancingCharge(balancingChargeYesNo = true, Some(BigDecimal(10000))))
        )
        .get

      incomeAndBalancingChargeCombined(userAnswers, Rentals) shouldEqual 90000
    }

    "under 85k if user selected total income is Under" in {
      val userAnswers = UserAnswers("test").set(TotalIncomePage, Under).get
      isTotalIncomeUnder85K(userAnswers, Rentals) shouldBe true
    }
    "over 85k if user selected total income is Over" in {
      val userAnswers = UserAnswers("test").set(TotalIncomePage, Over).get
      isTotalIncomeUnder85K(userAnswers, Rentals) shouldBe false
    }
    "under 85k if sum of all income section" in {
      val userAnswers = UserAnswers("test")
        .set(TotalIncomePage, Over)
        .flatMap(_.set(PropertyRentalIncomePage(Rentals), BigDecimal(80000)))
        .get
      isTotalIncomeUnder85K(userAnswers, Rentals) shouldBe true
    }
  }
}
