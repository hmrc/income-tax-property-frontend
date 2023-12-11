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

package pages

import models.UserAnswers
import play.api.libs.json.JsPath
import models.TotalIncomeUtils.isTotalIncomeUnder85K
import pages.propertyrentals.expenses.ConsolidatedExpensesPage

import scala.util.Try

case object IncomeFromPropertyRentalsPage extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "incomeFromPropertyRentals"

  override def cleanup(value: Option[BigDecimal], userAnswers: UserAnswers): Try[UserAnswers] = {
    if (isTotalIncomeUnder85K(userAnswers)) super.cleanup(value, userAnswers)
    else if (userAnswers.get(ConsolidatedExpensesPage).fold(false)(data => data.consolidatedExpensesYesNo))
      userAnswers.remove(ConsolidatedExpensesPage)
    else
      super.cleanup(value, userAnswers)
  }
}
