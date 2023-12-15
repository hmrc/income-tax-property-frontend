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

import models.TotalIncome.{Between, Under}
import pages.{CalculatedFigureYourselfPage, IncomeFromPropertyRentalsPage, OtherIncomeFromPropertyPage, ReversePremiumsReceivedPage, TotalIncomePage}
import pages.adjustments.BalancingChargePage
import pages.premiumlease.PremiumsGrantLeasePage

object TotalIncomeUtils {

  def totalIncome(userAnswers: UserAnswers): BigDecimal = {
    val incomeFromPropertyRentals = userAnswers.get(IncomeFromPropertyRentalsPage).getOrElse(BigDecimal(0))
    val leasePremiumCalculated = userAnswers.get(CalculatedFigureYourselfPage).flatMap(_.amount).getOrElse(BigDecimal(0))
    val reversePremiumsReceived = userAnswers.get(ReversePremiumsReceivedPage).flatMap(_.amount).getOrElse(BigDecimal(0))
    val premiumsGrantLease = userAnswers.get(PremiumsGrantLeasePage).getOrElse(BigDecimal(0))
    val otherIncome = userAnswers.get(OtherIncomeFromPropertyPage).map(_.amount).getOrElse(BigDecimal(0))

    incomeFromPropertyRentals + leasePremiumCalculated + premiumsGrantLease + reversePremiumsReceived + otherIncome
  }

  def isTotalIncomeUnder85K(userAnswers: UserAnswers): Boolean = {
    val totalIncomeCapped = 85000
    userAnswers.get(IncomeFromPropertyRentalsPage) match {
      case Some(_) =>
        totalIncome(userAnswers) < BigDecimal(totalIncomeCapped)
      case None =>
        val userSelectedIncome = userAnswers.get(TotalIncomePage).getOrElse(0)
        userSelectedIncome == Under || userSelectedIncome == Between
    }
  }

  def maxPropertyIncomeAllowanceCombined(userAnswers: UserAnswers): BigDecimal = {
    val balancingCharge = userAnswers.get(BalancingChargePage).flatMap(_.balancingChargeAmount).getOrElse(BigDecimal(0))
    totalIncome(userAnswers) + balancingCharge
  }
}
