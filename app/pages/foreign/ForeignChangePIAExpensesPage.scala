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

package pages.foreign

import models._
import pages.PageConstants.aboutPath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object ForeignChangePIAExpensesPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ aboutPath(ForeignProperty) \ toString

  override def toString: String = "foreignChangePIAExpenses"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    val countryCodes: Array[String] =
      userAnswers
        .get(ForeignPropertySelectCountry)
        .flatMap(_.incomeCountries.map(_.map(_.code)))
        .getOrElse(Array.empty)

    countryCodes.foldLeft(Try(userAnswers)) { (acc, countryCode) =>
      for {
        ua <- acc
        ua1 <- ua.remove(ReadForeignPropertyAdjustments(countryCode))
        ua2 <- ua1.remove(ReadWriteForeignPropertyAllowances(countryCode))
        ua3 <- ua2.remove(ReadWriteStructuredBuildingAllowance(countryCode))
        ua4 <- ua3.remove(ReadForeignPropertyExpenses(countryCode))
        ua5 <- ua4.remove(ReadForeignPropertyIncome(countryCode))
        ua6 <- ua5.remove(ReadWriteForeignPropertyTax(countryCode))
      } yield ua6
    }
  }
}
