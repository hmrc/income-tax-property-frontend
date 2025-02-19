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

package models

import models.TotalIncome.{Between, Under}
import pages.foreign.TotalIncomePage
import pages.foreign.adjustments.ForeignBalancingChargePage
import pages.foreign.{CalculatedPremiumLeaseTaxablePage, ForeignPremiumsGrantLeasePage}
import pages.foreign.income.{ForeignPropertyRentalIncomePage, ForeignOtherIncomeFromPropertyPage}

object ForeignTotalIncomeUtils {

  def totalIncome(userAnswers: UserAnswers, countryCode: String): BigDecimal = {
    val propertyRentalIncome = userAnswers.get(ForeignPropertyRentalIncomePage(countryCode)).getOrElse(BigDecimal(0))
    val leasePremiumCalculated =
      userAnswers.get(CalculatedPremiumLeaseTaxablePage(countryCode)).flatMap(_.premiumsOfLeaseGrant).getOrElse(BigDecimal(0))
    val premiumsGrantLease =
      userAnswers.get(ForeignPremiumsGrantLeasePage(countryCode)).flatMap(_.premiumsOfLeaseGrant).getOrElse(BigDecimal(0))
    val otherIncome = userAnswers.get(ForeignOtherIncomeFromPropertyPage(countryCode)).getOrElse(BigDecimal(0))

    propertyRentalIncome + leasePremiumCalculated + premiumsGrantLease + otherIncome
  }

  def isTotalIncomeUnder85K(userAnswers: UserAnswers, countryCode: String): Boolean = {
    val totalIncomeCapped = 85000
    userAnswers.get(ForeignPropertyRentalIncomePage(countryCode)) match {
      case Some(_) =>
        totalIncome(userAnswers, countryCode) < BigDecimal(totalIncomeCapped)
      case None =>
        val userSelectedIncome = userAnswers.get(TotalIncomePage).getOrElse(0)
        userSelectedIncome == Under || userSelectedIncome == Between
    }
  }

  def incomeAndBalancingChargeCombined(userAnswers: UserAnswers, countryCode: String): BigDecimal = {
    val balancingCharge = userAnswers.get(ForeignBalancingChargePage(countryCode)).flatMap(_.balancingChargeAmount).getOrElse(BigDecimal(0))
    totalIncome(userAnswers, countryCode) + balancingCharge
  }

  def maxAllowedPIA(incomeAndBalancingChargeCombined: BigDecimal): BigDecimal =
    incomeAndBalancingChargeCombined min 1000

}
