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

import models.{UserAnswers, ForeignProperty, ReadWriteForeignPropertyAllowances, ReadForeignPropertyIncome, ReadForeignPropertyExpenses, ReadWriteStructuredBuildingAllowance, ReadWriteForeignPropertyTax, ReadForeignPropertyAdjustments, ForeignPropertySelectCountry}
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
      acc.flatMap { ua =>
        ua.remove(ReadForeignPropertyAdjustments(countryCode))
        ua.remove(ReadWriteForeignPropertyAllowances(countryCode))
        ua.remove(ReadWriteStructuredBuildingAllowance(countryCode))
        ua.remove(ReadForeignPropertyExpenses(countryCode))
        ua.remove(ReadForeignPropertyIncome(countryCode))
        ua.remove(ReadWriteForeignPropertyTax(countryCode))
      }
    }
  }
}
