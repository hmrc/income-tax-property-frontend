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

import models.{UserAnswers, ForeignProperty}
import pages.PageConstants.aboutPath
import pages.QuestionPage
import pages.foreign.adjustments.ForeignAdjustmentsCompletePage
import pages.foreign.allowances.ForeignAllowancesCompletePage
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import pages.foreign.income.ForeignIncomeSectionCompletePage
import pages.foreign.structurebuildingallowance.ForeignSbaCompletePage
import play.api.libs.json.JsPath
import service.CountryNamesDataSource
import uk.gov.hmrc.play.language.LanguageUtils

import scala.util.Try

case object ForeignChangePIAExpensesPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ aboutPath(ForeignProperty) \ toString

  override def toString: String = "foreignChangePIAExpenses"

  def clean(value: Option[Boolean], userAnswers: UserAnswers, country: String): Try[UserAnswers] = {
    value.map {
      case false =>
        userAnswers.remove(ForeignAdjustmentsCompletePage(country))
        userAnswers.remove(ForeignAllowancesCompletePage(country))
        userAnswers.remove(ForeignExpensesSectionCompletePage(country))
        userAnswers.remove(ForeignIncomeSectionCompletePage(country))
        userAnswers.remove(ForeignTaxSectionCompletePage(country))
        userAnswers.remove(ForeignSbaCompletePage(country))
      case _ => super.cleanup(value, userAnswers)
    }.getOrElse(super.cleanup(value, userAnswers))}
}
