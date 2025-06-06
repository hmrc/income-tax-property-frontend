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

package pages.ukandforeignproperty

import models.{RentalsAndRaRAbout, UKAndForeignProperty, UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses, UserAnswers}
import pages.PageConstants.aboutPath
import pages.QuestionPage
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import play.api.libs.json.JsPath

import scala.util.Try

case object UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage extends QuestionPage[UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses] {

  override def path: JsPath = JsPath \ aboutPath(UKAndForeignProperty) \ toString

  override def toString: String = "claimPropertyIncomeAllowanceOrExpenses"

  override def cleanup(value: Option[UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses], userAnswers: UserAnswers): Try[UserAnswers] =
    for {
      ua  <- userAnswers.remove(RentalsRaRAboutCompletePage)
      ua2 <- ua.remove(RentalsAndRaRAbout)
    } yield ua2
}

