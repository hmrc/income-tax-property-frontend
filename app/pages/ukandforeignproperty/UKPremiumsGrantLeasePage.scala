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

package pages.ukandforeignproperty

import models.TotalIncomeUtils.isTotalIncomeUnder85K
import models.ukAndForeign.UKPremiumsGrantLease
import models.{UKAndForeignProperty, UserAnswers}
import pages.PageConstants.aboutPath
import pages.QuestionPage
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.libs.json.JsPath

import scala.util.Try

object UKPremiumsGrantLeasePage extends QuestionPage[UKPremiumsGrantLease] {

  override def path: JsPath = JsPath \ aboutPath(UKAndForeignProperty) \ toString

  override def toString: String = "premiumsGrantLease"

  def calculateTaxableAmount(premiumAmount: BigDecimal, periods: Int): BigDecimal =
    (premiumAmount * (BigDecimal(50 - minusOne(periods)) / 50)).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def minusOne(periods: Int): Int = periods - 1

  override def cleanup(value: Option[UKPremiumsGrantLease], userAnswers: UserAnswers): Try[UserAnswers] =
    if (isTotalIncomeUnder85K(userAnswers, UKAndForeignProperty)) {
      super.cleanup(value, userAnswers)
    } else if (
      userAnswers
        .get(ConsolidatedExpensesPage(UKAndForeignProperty))
        .fold(false)(data => data.consolidatedExpensesYesOrNo)
    ) {
      userAnswers.remove(ConsolidatedExpensesPage(UKAndForeignProperty))
    } else {
      super.cleanup(value, userAnswers)
    }

}
