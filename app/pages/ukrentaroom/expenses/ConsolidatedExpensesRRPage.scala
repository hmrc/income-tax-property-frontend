/*
 * Copyright 2024 HM Revenue & Customs
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

package pages.ukrentaroom.expenses

import models.{ConsolidatedRRExpenses, UserAnswers}
import pages.{PageConstants, QuestionPage}
import play.api.libs.json.JsPath

import scala.util.Try

case object ConsolidatedExpensesRRPage extends QuestionPage[ConsolidatedRRExpenses] {

  override def path: JsPath = JsPath \ PageConstants.rentARoomExpense \ toString

  override def toString: String = "consolidatedExpenses"

  override def cleanup(value: Option[ConsolidatedRRExpenses], userAnswers: UserAnswers): Try[UserAnswers] =
    value
      .map {
        case ConsolidatedRRExpenses(false, _) => super.cleanup(value, userAnswers)

        case ConsolidatedRRExpenses(true, _) =>
          for {

            withoutRentRatesAndInsurance     <- userAnswers.remove(RentsRatesAndInsuranceRRPage)
            withoutRepairAndMaintenanceCosts <- withoutRentRatesAndInsurance.remove(RepairsAndMaintenanceCostsRRPage)
            withoutLegalManagementOtherFees  <- withoutRepairAndMaintenanceCosts.remove(LegalManagementOtherFeeRRPage)
            withoutCostOfServicesProvided    <- withoutLegalManagementOtherFees.remove(CostOfServicesProvidedRRPage)
            withoutResidentialPropertyFinanceCosts <-
              withoutCostOfServicesProvided.remove(ResidentialPropertyFinanceCostsRRPage)
            withoutAllPreviousAnswers <-
              withoutResidentialPropertyFinanceCosts.remove(OtherPropertyExpensesRRPage)
          } yield withoutAllPreviousAnswers
      }
      .getOrElse(super.cleanup(value, userAnswers))
}
