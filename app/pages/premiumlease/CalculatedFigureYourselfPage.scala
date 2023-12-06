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

import models.TotalIncomeUtils.isTotalIncomeUnder85K
import models.{CalculatedFigureYourself, UserAnswers}
import pages.premiumlease.{PremiumsGrantLeasePage, RecievedGrantLeaseAmountPage, YearLeaseAmountPage}
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.libs.json.JsPath

import scala.util.Try

case object CalculatedFigureYourselfPage extends QuestionPage[CalculatedFigureYourself] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "calculatedFigureYourself"

  override def cleanup(value: Option[CalculatedFigureYourself], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case CalculatedFigureYourself(false, _)  => super.cleanup(value, userAnswers)
      case CalculatedFigureYourself(true, amount) =>
        if (!isTotalIncomeUnder85K(userAnswers) && userAnswers.get(ConsolidatedExpensesPage).fold(false)(data => data.consolidatedExpenses))
          for {
            rGLAP <- userAnswers.remove(RecievedGrantLeaseAmountPage)
            yLAP <- rGLAP.remove(YearLeaseAmountPage)
            pGLP <- yLAP.remove(PremiumsGrantLeasePage)
            cE <- pGLP.remove(ConsolidatedExpensesPage)
          } yield cE else
        for {
          rGLAP <- userAnswers.remove(RecievedGrantLeaseAmountPage)
          yLAP <- rGLAP.remove(YearLeaseAmountPage)
          pGLP <- yLAP.remove(PremiumsGrantLeasePage)
        } yield pGLP
    }.getOrElse(super.cleanup(value, userAnswers))
}
