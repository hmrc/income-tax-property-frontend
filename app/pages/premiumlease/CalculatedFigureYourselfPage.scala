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

package pages.premiumlease

import models.TotalIncomeUtils.isTotalIncomeUnder85K
import models.{CalculatedFigureYourself, PropertyType, UserAnswers}
import pages.PageConstants.incomePath
import pages.QuestionPage
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.libs.json.JsPath

import scala.util.Try

case class CalculatedFigureYourselfPage(propertyType: PropertyType) extends QuestionPage[CalculatedFigureYourself] {

  override def path: JsPath = JsPath \ incomePath(propertyType) \ toString

  override def toString: String = "calculatedFigureYourself"

  override def cleanup(value: Option[CalculatedFigureYourself], userAnswers: UserAnswers): Try[UserAnswers] =
    value
      .map {
        case CalculatedFigureYourself(false, _) => super.cleanup(value, userAnswers)
        case CalculatedFigureYourself(true, _) =>
          val consolidatedExpensesYesOrNo: Boolean = userAnswers
            .get(ConsolidatedExpensesPage(propertyType))
            .fold(false)(data => data.consolidatedExpensesYesOrNo)

          if (!isTotalIncomeUnder85K(userAnswers, propertyType) && consolidatedExpensesYesOrNo) {
            for {
              rGLAP <- userAnswers.remove(ReceivedGrantLeaseAmountPage(propertyType))
              yLAP  <- rGLAP.remove(YearLeaseAmountPage(propertyType))
              pGLP  <- yLAP.remove(PremiumsGrantLeasePage(propertyType))
              cE    <- pGLP.remove(ConsolidatedExpensesPage(propertyType))
            } yield cE
          } else {
            for {
              rGLAP <- userAnswers.remove(ReceivedGrantLeaseAmountPage(propertyType))
              yLAP  <- rGLAP.remove(YearLeaseAmountPage(propertyType))
              pGLP  <- yLAP.remove(PremiumsGrantLeasePage(propertyType))
            } yield pGLP
          }
      }
      .getOrElse(super.cleanup(value, userAnswers))
}
