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

package service

import controllers.foreign.allowances.routes._
import controllers.foreign.adjustments.routes._
import controllers.foreign.expenses.routes._
import controllers.foreign.income.routes._
import controllers.foreign.routes._
import models._
import pages.QuestionPage
import pages.foreign.allowances.ForeignAllowancesCompletePage
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import pages.foreign.income.ForeignIncomeSectionCompletePage
import pages.foreign.adjustments.ForeignAdjustmentsCompletePage
import pages.foreign.{ForeignSelectCountriesCompletePage, ForeignTaxSectionCompletePage}
import play.api.mvc.Call
import queries.Gettable

import javax.inject.Inject

class ForeignCYADiversionService @Inject() {

  def redirectCallToCYAIfFinished(
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    journeyName: String,
    maybeCountryCode: Option[String]
  )(
    block: => Call
  ): Call =
    redirectToCYAIfFinished(taxYear, userAnswers, journeyName, maybeCountryCode, NormalMode)(block)(identity[Call])

  def selectCountry[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, Option[String]), T] = {
    case (NormalMode, ForeignCYADiversionService.SELECT_COUNTRY, _) =>
      divert(ForeignSelectCountriesCompletePage, userAnswers, block)(
        cyaDiversion = controllers.foreign.routes.ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
  }

  def foreignTax[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, Option[String]), T] = {
    case (NormalMode, ForeignCYADiversionService.FOREIGN_TAX, Some(countryCode)) =>
      divert(ForeignTaxSectionCompletePage(countryCode), userAnswers, block)(
        cyaDiversion = ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      )(transform)
  }

  def expenses[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, Option[String]), T] = {
    case (NormalMode, ForeignCYADiversionService.EXPENSES, Some(countryCode)) =>
      divert(ForeignExpensesSectionCompletePage(countryCode), userAnswers, block)(
        cyaDiversion = ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      )(transform)
  }

  def allowances[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, Option[String]), T] = {
    case (NormalMode, ForeignCYADiversionService.ALLOWANCES, Some(countryCode)) =>
      divert(ForeignAllowancesCompletePage(countryCode), userAnswers, block)(
        cyaDiversion = ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      )(transform)
  }

  def income[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, Option[String]), T] = {
    case (NormalMode, ForeignCYADiversionService.INCOME, Some(countryCode)) =>
      divert(ForeignIncomeSectionCompletePage(countryCode), userAnswers, block)(
        cyaDiversion = ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      )(transform)
  }

  def adjustments[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, Option[String]), T] = {
    case (NormalMode, ForeignCYADiversionService.ADJUSTMENTS, Some(countryCode)) =>
      divert(ForeignAdjustmentsCompletePage(countryCode), userAnswers, block)(
        cyaDiversion = ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      )(transform)
  }

  def forOther[T](block: => T): PartialFunction[(Mode, String, Option[String]), T] = { case _ =>
    block
  }

  def redirectToCYAIfFinished[T](
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    journeyName: String,
    maybeCountryCode: Option[String],
    mode: Mode
  )(
    block: => T
  )(transform: Call => T): T =
    userAnswers.fold(
      forOther(block)(mode, journeyName, maybeCountryCode)
    )(ua => redirectToCYAIfFinished(taxYear, ua, journeyName, maybeCountryCode, mode)(block)(transform))

  def redirectToCYAIfFinished[T](
    taxYear: Int,
    userAnswers: UserAnswers,
    journeyName: String,
    maybeCountryCode: Option[String],
    mode: Mode
  )(
    block: => T
  )(transform: Call => T): T =
    selectCountry(taxYear, userAnswers)(block)(transform)
      .orElse(
        foreignTax(taxYear, userAnswers)(block)(transform)
      )
      .orElse(
        allowances(taxYear, userAnswers)(block)(transform)
      )
      .orElse(
        income(taxYear, userAnswers)(block)(transform)
      )
      .orElse(
        expenses(taxYear, userAnswers)(block)(transform)
      )
      .orElse(
        adjustments(taxYear, userAnswers)(block)(transform)
      )
      .orElse(forOther(block))((mode, journeyName, maybeCountryCode))

  private def divert[T](questionPage: QuestionPage[Boolean], userAnswers: UserAnswers, block: => T)(
    cyaDiversion: Call
  )(transform: Call => T): T =
    if (isJourneyFinished(userAnswers, questionPage)) {
      transform(cyaDiversion)
    } else {
      block
    }

  private def isJourneyFinished(userAnswers: UserAnswers, page: Gettable[Boolean]): Boolean =
    userAnswers.get(page).getOrElse(false)
}

object ForeignCYADiversionService {
  val SELECT_COUNTRY = "select-country"
  val FOREIGN_TAX = "foreign-tax"
  val INCOME = "income"
  val EXPENSES = "expenses"
  val ALLOWANCES = "allowances"
  val ADJUSTMENTS = "adjustments"
}
