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

package pages.propertyrentals.income

import models.TotalIncomeUtils.isTotalIncomeUnder85K
import models.{PropertyType, ReversePremiumsReceived, UserAnswers}
import pages.PageConstants.incomePath
import pages.QuestionPage
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.libs.json.JsPath

import scala.util.Try

case class ReversePremiumsReceivedPage(propertyType: PropertyType) extends QuestionPage[ReversePremiumsReceived] {

  override def path: JsPath = JsPath \ incomePath(propertyType) \ toString

  override def toString: String = "reversePremiumsReceived"

  override def cleanup(value: Option[ReversePremiumsReceived], userAnswers: UserAnswers): Try[UserAnswers] =
    if (isTotalIncomeUnder85K(userAnswers, propertyType)) {
      super.cleanup(value, userAnswers)
    } else if (
      userAnswers.get(ConsolidatedExpensesPage(propertyType)).fold(false)(data => data.consolidatedExpensesYesOrNo)
    ) {
      userAnswers.remove(ConsolidatedExpensesPage(propertyType))
    } else {
      super.cleanup(value, userAnswers)
    }
}
