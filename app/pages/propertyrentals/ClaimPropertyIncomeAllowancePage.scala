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

package pages.propertyrentals

import models.{PropertyType, RentalsRentARoom, UserAnswers}
import pages.PageConstants.aboutPath
import pages.QuestionPage
import pages.adjustments.{BalancingChargePage, PrivateUseAdjustmentPage, PropertyIncomeAllowancePage, ResidentialFinanceCostPage, UnusedResidentialFinanceCostPage}
import pages.allowances.BusinessPremisesRenovationPage
import pages.ukrentaroom.adjustments.RaRUnusedResidentialCostsPage
import play.api.libs.json.JsPath
import viewmodels.checkAnswers.ukrentaroom.adjustments.UnusedResidentialPropertyFinanceCostsBroughtFwdSummary

import scala.util.Try

case class ClaimPropertyIncomeAllowancePage(propertyType: PropertyType) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ aboutPath(propertyType) \ toString

  override def toString: String = "claimPropertyIncomeAllowanceYesOrNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    for {
      answersWithoutPrivateUseAdjustment <-
        userAnswers.remove(PrivateUseAdjustmentPage(RentalsRentARoom))
      answersWithoutBalancingCharge <-
        answersWithoutPrivateUseAdjustment.remove(BalancingChargePage(RentalsRentARoom))
      answersWithoutPropertyIncomeAllowance <-
        answersWithoutBalancingCharge.remove(PropertyIncomeAllowancePage(RentalsRentARoom))
      answerWithoutBusinessPremisesRenovation <-
        answersWithoutPropertyIncomeAllowance.remove(BusinessPremisesRenovationPage(RentalsRentARoom))
      answersWithoutResidentialFinancialCosts <-
        answerWithoutBusinessPremisesRenovation.remove(ResidentialFinanceCostPage(RentalsRentARoom))
      result <- answersWithoutResidentialFinancialCosts.remove(UnusedResidentialFinanceCostPage(RentalsRentARoom))
    } yield result

  /*
    val previousValue: Option[Boolean] = userAnswers.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom))

    (value, previousValue) match {
      // If the option the user has selected has not changed i.e. it was previous true and is now true or it was false
      // and now is false again, then don't do anything
      case (Some(true), Some(true)) | (Some(false), Some(false)) => super.cleanup(value, userAnswers)
      // If the user has now selected PIA when they have previously selected No, claim expenses then the previous answers
      // need to be deleted
      case (Some(true), Some(false)) => ???
      // If the user has now selected No, claim expense when they have previously selected Yes, claim property income allowance
      // then the previous answers need to be deleted
      case (Some(false), Some(true)) =>
        for {
          answersWithoutPrivateUseAdjustment <-
            userAnswers.remove(PrivateUseAdjustmentPage(RentalsRentARoom))
          answersWithoutBalancingCharge <-
            answersWithoutPrivateUseAdjustment.remove(BalancingChargePage(RentalsRentARoom))
          answersWithoutPropertyIncomeAllowance <-
            answersWithoutBalancingCharge.remove(PropertyIncomeAllowancePage(RentalsRentARoom))
          answersWithoutResidentialFinanceCost <-
            answersWithoutPropertyIncomeAllowance.remove(ResidentialFinanceCostPage(RentalsRentARoom))
          result <-
            answersWithoutResidentialFinanceCost.remove(BusinessPremisesRenovationPage(RentalsRentARoom))
        } yield result
    }
   */
}
