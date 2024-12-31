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

package pages.foreign.expenses

import models.{ConsolidatedOrIndividualExpenses, ForeignProperty, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import pages.PageConstants.expensesPath

import scala.util.Try

case class ConsolidatedOrIndividualExpensesPage(countryCode: String)
    extends QuestionPage[ConsolidatedOrIndividualExpenses] {

  override def path: JsPath = JsPath \ expensesPath(ForeignProperty) \ countryCode.toUpperCase \ toString

  override def toString: String = "consolidatedExpenses"

  override def cleanup(value: Option[ConsolidatedOrIndividualExpenses], userAnswers: UserAnswers): Try[UserAnswers] =
    value
      .map {
        case ConsolidatedOrIndividualExpenses(false, _) => super.cleanup(value, userAnswers)

        case ConsolidatedOrIndividualExpenses(true, _) =>
          for {
            insurance <- userAnswers.remove(ForeignRentsRatesAndInsurancePage(countryCode))
            repairs   <- insurance.remove(ForeignPropertyRepairsAndMaintenancePage(countryCode))
            cost      <- repairs.remove(ForeignCostsOfServicesProvidedPage(countryCode))
            finance   <- cost.remove(ForeignNonResidentialPropertyFinanceCostsPage(countryCode))
            fees      <- finance.remove(ForeignProfessionalFeesPage(countryCode))
            other     <- fees.remove(ForeignOtherAllowablePropertyExpensesPage(countryCode))
          } yield other
      }
      .getOrElse(super.cleanup(value, userAnswers))
}
