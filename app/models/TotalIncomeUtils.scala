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
import pages.TotalIncomePage
import pages.adjustments.BalancingChargePage
import pages.premiumlease.{CalculatedFigureYourselfPage, PremiumsGrantLeasePage}
import pages.propertyrentals.income.{PropertyRentalIncomePage, OtherIncomeFromPropertyPage, ReversePremiumsReceivedPage}

object TotalIncomeUtils {

  def totalIncome(userAnswers: UserAnswers, propertyType: PropertyType): BigDecimal = {
    val propertyRentalIncome = userAnswers.get(PropertyRentalIncomePage(propertyType)).getOrElse(BigDecimal(0))
    val leasePremiumCalculated =
      userAnswers.get(CalculatedFigureYourselfPage(propertyType)).flatMap(_.amount).getOrElse(BigDecimal(0))
    val reversePremiumsReceived =
      userAnswers.get(ReversePremiumsReceivedPage(propertyType)).flatMap(_.reversePremiums).getOrElse(BigDecimal(0))
    val premiumsGrantLease =
      userAnswers.get(PremiumsGrantLeasePage(propertyType)).flatMap(_.premiumsGrantLease).getOrElse(BigDecimal(0))
    val otherIncome = userAnswers.get(OtherIncomeFromPropertyPage(propertyType)).getOrElse(BigDecimal(0))

    propertyRentalIncome + leasePremiumCalculated + premiumsGrantLease + reversePremiumsReceived + otherIncome
  }

  def isTotalIncomeUnder85K(userAnswers: UserAnswers, propertyType: PropertyType): Boolean = {
    val totalIncomeCapped = 85000
    userAnswers.get(PropertyRentalIncomePage(propertyType)) match {
      case Some(_) =>
        totalIncome(userAnswers, propertyType) < BigDecimal(totalIncomeCapped)
      case None =>
        val userSelectedIncome = userAnswers.get(TotalIncomePage).getOrElse(0)
        userSelectedIncome == Under || userSelectedIncome == Between
    }
  }

  def incomeAndBalancingChargeCombined(userAnswers: UserAnswers, propertyType: PropertyType): BigDecimal = {
    val balancingCharge = userAnswers.get(BalancingChargePage(propertyType)).flatMap(_.balancingChargeAmount).getOrElse(BigDecimal(0))
    totalIncome(userAnswers, propertyType) + balancingCharge
  }

  def maxAllowedPIA(incomeAndBalancingChargeCombined: BigDecimal): BigDecimal =
    incomeAndBalancingChargeCombined min 1000

}
