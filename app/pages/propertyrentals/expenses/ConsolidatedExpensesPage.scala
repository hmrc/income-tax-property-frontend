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

package pages.propertyrentals.expenses

import models.{ConsolidatedExpenses, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object ConsolidatedExpensesPage extends QuestionPage[ConsolidatedExpenses] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "consolidatedExpenses"

  override def cleanup(value: Option[ConsolidatedExpenses], userAnswers: UserAnswers): Try[UserAnswers] = {
    value.map {
      case ConsolidatedExpenses(false, _) => super.cleanup(value, userAnswers)

      case ConsolidatedExpenses(true, _) =>
        for {
          rRRAI <- userAnswers.remove(RentsRatesAndInsurancePage)
          rAMC <- userAnswers.remove(RepairsAndMaintenanceCostsPage)
          cOSP <- userAnswers.remove(CostsOfServicesProvidedPage)
          lI <- userAnswers.remove(LoanInterestPage)
          pBTC <- userAnswers.remove(PropertyBusinessTravelCostsPage)
          oPF <- userAnswers.remove(OtherProfessionalFeesPage)
          oAPE <- userAnswers.remove(OtherAllowablePropertyExpensesPage)
        } yield oAPE
    }.getOrElse(super.cleanup(value, userAnswers))
  }
}
