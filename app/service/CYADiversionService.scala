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

import controllers.about.routes._
import controllers.adjustments.routes._
import controllers.allowances.routes._
import controllers.enhancedstructuresbuildingallowance.routes._
import controllers.propertyrentals.expenses.routes._
import controllers.propertyrentals.income.routes._
import controllers.propertyrentals.routes._
import controllers.structuresbuildingallowance.routes._
import controllers.ukrentaroom.adjustments.routes._
import controllers.ukrentaroom.allowances.routes._
import controllers.ukrentaroom.expenses.routes._
import models._
import pages.{QuestionPage, AboutPropertyCompletePage}
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance.{ClaimEsbaPage, EsbaSectionFinishedPage, EsbaClaimPage}
import pages.propertyrentals.AboutPropertyRentalsSectionFinishedPage
import pages.propertyrentals.expenses._
import pages.propertyrentals.income.IncomeSectionFinishedPage
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import pages.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompletePage
import pages.rentalsandrentaroom.allowances.RentalsRaRAllowancesCompletePage
import pages.rentalsandrentaroom.expenses.RentalsRaRExpensesCompletePage
import pages.rentalsandrentaroom.income.RentalsRaRIncomeCompletePage
import pages.structurebuildingallowance._
import pages.ukrentaroom.AboutSectionCompletePage
import pages.ukrentaroom.adjustments._
import pages.ukrentaroom.allowances.RaRAllowancesCompletePage
import pages.ukrentaroom.expenses._
import play.api.mvc.Call
import queries.Gettable

import javax.inject.Inject

class CYADiversionService @Inject() {
  private val EXPENSES = "expenses"
  private val INCOME = "income"
  private val ALLOWANCES = "allowances"
  private val ADJUSTMENTS = "adjustments"
  private val ESBA = "esba"
  private val SBA = "sba"
  private val ABOUT = "about"

  def redirectCallToCYAIfFinished(
    taxYear: Int,
    userAnswers: UserAnswers,
    journeyName: String,
    propertyType: PropertyType
  )(
    block: => Call
  ): Call =
    redirectToCYAIfFinished(taxYear, userAnswers, journeyName, propertyType, NormalMode)(block)(identity[Call])

  def expenses[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, PropertyType), T] = {
    case (NormalMode, EXPENSES, Rentals) =>
      divert(taxYear, ExpensesSectionFinishedPage, userAnswers, block)(
        cyaDiversion = ExpensesCheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, EXPENSES, RentARoom) =>
      divert(taxYear, ExpensesRRSectionCompletePage, userAnswers, block)(
        cyaDiversion = ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, EXPENSES, RentalsRentARoom) =>
      divert(taxYear, RentalsRaRExpensesCompletePage, userAnswers, block)(
        cyaDiversion = controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController
          .onPageLoad(taxYear)
      )(transform)
  }

  def allowances[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, PropertyType), T] = {
    case (NormalMode, ALLOWANCES, Rentals) =>
      divert(taxYear, AllowancesSectionFinishedPage, userAnswers, block)(
        cyaDiversion = AllowancesCheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, ALLOWANCES, RentARoom) =>
      divert(taxYear = taxYear, userAnswers = userAnswers, questionPage = RaRAllowancesCompletePage, block = block)(
        cyaDiversion = RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, ALLOWANCES, RentalsRentARoom) =>
      divert(
        taxYear = taxYear,
        userAnswers = userAnswers,
        questionPage = RentalsRaRAllowancesCompletePage,
        block = block
      )(
        cyaDiversion =
          controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
            .onPageLoad(taxYear)
      )(transform)
  }
  def about[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, PropertyType), T] = {
    case (NormalMode, ABOUT, UKProperty) =>
      divert(taxYear, AboutPropertyCompletePage, userAnswers, block)(
        cyaDiversion = CheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, ABOUT, Rentals) =>
      divert(taxYear, AboutPropertyRentalsSectionFinishedPage, userAnswers, block)(
        cyaDiversion = PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, ABOUT, RentalsRentARoom) =>
      divert(taxYear, RentalsRaRAboutCompletePage, userAnswers, block)(
        cyaDiversion =
          controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, ABOUT, RentARoom) =>
      divert(taxYear, AboutSectionCompletePage, userAnswers, block)(
        cyaDiversion = controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
  }

  def adjustments[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, PropertyType), T] = {
    case (NormalMode, ADJUSTMENTS, Rentals) =>
      divert(taxYear, RentalsAdjustmentsCompletePage, userAnswers, block)(
        cyaDiversion = AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, ADJUSTMENTS, RentARoom) =>
      divert(taxYear = taxYear, userAnswers = userAnswers, questionPage = RaRAdjustmentsCompletePage, block = block)(
        cyaDiversion = RaRAdjustmentsCYAController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, ADJUSTMENTS, RentalsRentARoom) =>
      divert(
        taxYear = taxYear,
        userAnswers = userAnswers,
        questionPage = RentalsRaRAdjustmentsCompletePage,
        block = block
      )(
        cyaDiversion =
          controllers.rentalsandrentaroom.adjustments.routes.RentalsAndRentARoomAdjustmentsCheckYourAnswersController
            .onPageLoad(taxYear)
      )(transform)
  }

  def sbasAndEsbas[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, PropertyType), T] = {
    case (NormalMode, SBA, propertyType) =>
      divert(taxYear, SbaSectionFinishedPage(propertyType), userAnswers, block)(
        cyaDiversion =
          if (
            !userAnswers.get(ClaimStructureBuildingAllowancePage(propertyType)).getOrElse(false) ||
            userAnswers.get(StructureBuildingAllowanceClaimPage(0, propertyType)).isEmpty
          ) {
            ClaimSbaCheckYourAnswersController.onPageLoad(taxYear, propertyType)
          } else {
            SbaClaimsController.onPageLoad(taxYear, propertyType)
          }
      )(transform)
    case (NormalMode, ESBA, propertyType) =>
      divert(taxYear, EsbaSectionFinishedPage(propertyType), userAnswers, block)(
        cyaDiversion =
          if (
            !userAnswers.get(ClaimEsbaPage(propertyType)).getOrElse(false) ||
            userAnswers.get(EsbaClaimPage(0, propertyType)).isEmpty
          ) {
            ClaimEsbaCheckYourAnswersController.onPageLoad(taxYear, propertyType)
          } else {
            EsbaClaimsController.onPageLoad(taxYear, propertyType)
          }
      )(transform)
  }

  def income[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String, PropertyType), T] = {
    case (NormalMode, INCOME, Rentals) =>
      divert(taxYear, IncomeSectionFinishedPage, userAnswers, block)(
        cyaDiversion = PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
    case (NormalMode, INCOME, RentalsRentARoom) =>
      divert(taxYear, RentalsRaRIncomeCompletePage, userAnswers, block)(
        cyaDiversion = controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
          .onPageLoad(taxYear)
      )(transform)
  }

  def forOther[T](block: => T): PartialFunction[(Mode, String, PropertyType), T] = { case _ =>
    block
  }

  def redirectToCYAIfFinished[T](
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    journeyName: String,
    propertyType: PropertyType,
    mode: Mode
  )(
    block: => T
  )(transform: Call => T): T =
    userAnswers.fold(
      forOther(block)(mode, journeyName, propertyType)
    )(ua => redirectToCYAIfFinished(taxYear, ua, journeyName, propertyType, mode)(block)(transform))

  def redirectToCYAIfFinished[T](
    taxYear: Int,
    userAnswers: UserAnswers,
    journeyName: String,
    propertyType: PropertyType,
    mode: Mode
  )(
    block: => T
  )(transform: Call => T): T =
    about(taxYear, userAnswers)(block)(transform)
      .orElse(
        allowances(taxYear, userAnswers)(block)(transform)
      )
      .orElse(
        sbasAndEsbas(taxYear, userAnswers)(block)(transform)
      )
      .orElse(
        income(taxYear, userAnswers)(block)(transform)
      )
      .orElse(
        allowances(taxYear, userAnswers)(block)(transform)
      )
      .orElse(
        adjustments(taxYear, userAnswers)(block)(transform)
      )
      .orElse(
        expenses(taxYear, userAnswers)(block)(transform)
      )
      .orElse(forOther(block))((mode, journeyName, propertyType))

  private def divert[T](taxYear: Int, questionPage: QuestionPage[Boolean], userAnswers: UserAnswers, block: => T)(
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
