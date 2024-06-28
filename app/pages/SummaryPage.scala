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

import models.{NormalMode, UKPropertySelect, UserAnswers}
import pages.adjustments.PrivateUseAdjustmentPage
import pages.allowances.AllowancesSectionFinishedPage
import pages.enhancedstructuresbuildingallowance.EsbaQualifyingDatePage
import pages.propertyrentals.expenses.{ConsolidatedExpensesPage, RentsRatesAndInsurancePage}
import pages.propertyrentals.income.IncomeSectionFinishedPage
import pages.propertyrentals.expenses.ExpensesSectionFinishedPage
import pages.propertyrentals.income.IsNonUKLandlordPage
import pages.propertyrentals.{ClaimPropertyIncomeAllowancePage, ExpensesLessThan1000Page}
import pages.structurebuildingallowance.StructureBuildingQualifyingDatePage
import pages.ukrentaroom.adjustments.{RaRAdjustmentsCompletePage, RaRBalancingChargePage}
import pages.ukrentaroom.allowances.{RaRAllowancesCompletePage, RaRCapitalAllowancesForACarPage, RaRElectricChargePointAllowanceForAnEVPage}
import pages.ukrentaroom.expenses.{ConsolidatedExpensesRRPage, ExpensesRRSectionCompletePage, RentsRatesAndInsuranceRRPage}
import pages.ukrentaroom.{AboutSectionCompletePage, ClaimExpensesOrRRRPage, UkRentARoomJointlyLetPage}
import viewmodels.summary.TaskListTag.TaskListTag
import viewmodels.summary.{TaskListItem, TaskListTag}

case object SummaryPage {
  def createUkPropertyRows(
    userAnswers: Option[UserAnswers],
    taxYear: Int,
    cashOrAccruals: Boolean
  ): Seq[TaskListItem] = {
    val propertyRentalsAbout: TaskListItem = propertyRentalsAboutItem(userAnswers, taxYear)
    val propertyRentalsIncome: TaskListItem = propertyRentalsIncomeItem(userAnswers, taxYear)
    val propertyRentalsExpenses: TaskListItem = propertyRentalsExpensesItem(userAnswers, taxYear)
    val propertyAllowances: TaskListItem = propertyAllowancesItem(taxYear, userAnswers)
    val structuresAndBuildingAllowance: TaskListItem = structuresAndBuildingAllowanceItem(userAnswers, taxYear)
    val propertyRentalsAdjustments: TaskListItem = propertyRentalsAdjustmentsItem(userAnswers, taxYear)
    val enhancedStructuresAndBuildingAllowance: TaskListItem = rentalsEsbaItem(userAnswers, taxYear)

    val claimPropertyIncomeAllowance = userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage))
    val isPropertyRentalsSelected =
      userAnswers.exists(_.get(UKPropertyPage).exists(_.contains(UKPropertySelect.PropertyRentals)))

    if (isPropertyRentalsSelected) {
      claimPropertyIncomeAllowance
        .collect {
          case true => Seq(propertyRentalsAbout, propertyRentalsIncome, propertyRentalsAdjustments)
          case false if cashOrAccruals =>
            Seq(
              propertyRentalsAbout,
              propertyRentalsIncome,
              propertyRentalsExpenses,
              propertyAllowances,
              structuresAndBuildingAllowance,
              enhancedStructuresAndBuildingAllowance,
              propertyRentalsAdjustments
            )
          case false =>
            Seq(
              propertyRentalsAbout,
              propertyRentalsIncome,
              propertyRentalsExpenses,
              propertyAllowances,
              propertyRentalsAdjustments
            )
        }
        .getOrElse(Seq(propertyRentalsAbout))
    } else {
      Seq.empty[TaskListItem]
    }
  }

  def createUkRentARoomRows(userAnswers: Option[UserAnswers], taxYear: Int): Seq[TaskListItem] = {
    val ukRentARoomAbout: TaskListItem = ukRentARoomAboutItem(userAnswers, taxYear)
    val ukRentARoomExpenses: TaskListItem = ukRentARoomExpensesItem(userAnswers, taxYear)
    val ukRentARoomAllowances: TaskListItem = ukRentARoomAllowancesItem(userAnswers, taxYear)
    val ukRentARoomAdjustments: TaskListItem = ukRentARoomAdjustmentsItem(userAnswers, taxYear)
    val isRentARoomSelected = userAnswers.exists(_.get(UKPropertyPage).exists(_.contains(UKPropertySelect.RentARoom)))

    val claimRentARoomRelief = userAnswers.flatMap(_.get(ClaimExpensesOrRRRPage)).map(_.claimRRROrExpenses)
    if (isRentARoomSelected) {
      claimRentARoomRelief
        .collect {
          case true  => Seq(ukRentARoomAbout)
          case false => Seq(ukRentARoomAbout, ukRentARoomExpenses, ukRentARoomAllowances, ukRentARoomAdjustments)
        }
        .getOrElse(Seq(ukRentARoomAbout))
    } else {
      Seq.empty[TaskListItem]
    }
  }

  def propertyAboutItems(userAnswers: Option[UserAnswers], taxYear: Int): Seq[TaskListItem] =
    Seq(
      TaskListItem(
        "summary.about",
        controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear),
        if (userAnswers.flatMap(_.get(TotalIncomePage)).isDefined) TaskListTag.InProgress else TaskListTag.NotStarted,
        "property_about_link"
      )
    )

  private def rentalsEsbaItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.enhancedStructuresAndBuildingAllowance",
      controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController.onPageLoad(taxYear, NormalMode),
      if (userAnswers.flatMap(_.get(EsbaQualifyingDatePage(0))).isDefined) {
        TaskListTag.InProgress
      } else {
        TaskListTag.NotStarted
      },
      "rentals_enhanced_structures_and_building_allowance_link"
    )

  private def propertyRentalsAdjustmentsItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.adjustments",
      controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear),
      if (userAnswers.flatMap(_.get(PrivateUseAdjustmentPage)).isDefined) {
        TaskListTag.InProgress
      } else {
        TaskListTag.NotStarted
      },
      "rentals_adjustments_link"
    )

  private def structuresAndBuildingAllowanceItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.structuresAndBuildingAllowance",
      controllers.structuresbuildingallowance.routes.ClaimStructureBuildingAllowanceController
        .onPageLoad(taxYear, NormalMode),
      if (userAnswers.flatMap(_.get(StructureBuildingQualifyingDatePage(0))).isDefined) {
        TaskListTag.InProgress
      } else {
        TaskListTag.NotStarted
      },
      "rentals_structures_and_building_allowance_link"
    )

  private def propertyAllowancesItem(taxYear: Int, userAnswers: Option[UserAnswers]) =
    TaskListItem(
      "summary.allowances",
      controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear),
      userAnswers
        .flatMap { answers =>
          answers.get(AllowancesSectionFinishedPage).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_allowances_link"
    )

  private def propertyRentalsExpensesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.expenses",
      controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear),
      userAnswers
        .flatMap { answers =>
          answers.get(ExpensesSectionFinishedPage).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_expenses_link"
    )

  private def propertyRentalsIncomeItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.income",
      controllers.propertyrentals.income.routes.PropertyIncomeStartController.onPageLoad(taxYear),
      userAnswers
        .flatMap { answers =>
          answers.get(IncomeSectionFinishedPage).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_income_link"
    )

  private def propertyRentalsAboutItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.about",
      controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
      if (userAnswers.flatMap(_.get(ExpensesLessThan1000Page)).isDefined) TaskListTag.InProgress
      else TaskListTag.NotStarted,
      "rentals_about_link"
    )

  private def ukRentARoomAboutItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.about",
      controllers.ukrentaroom.routes.UkRentARoomJointlyLetController.onPageLoad(taxYear, NormalMode), {
        val sectionFinished = userAnswers.flatMap(_.get(AboutSectionCompletePage))

        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(UkRentARoomJointlyLetPage)).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rent_a_room_about_link"
    )

  private def ukRentARoomExpensesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.expenses",
      controllers.ukrentaroom.expenses.routes.UkRentARoomExpensesIntroController.onPageLoad(taxYear), {

        val sectionFinished = userAnswers.flatMap(_.get(ExpensesRRSectionCompletePage))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (
            userAnswers
              .flatMap(_.get(ConsolidatedExpensesRRPage))
              .isDefined || userAnswers.flatMap(_.get(RentsRatesAndInsuranceRRPage)).isDefined
          ) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rent_a_room_expenses_link"
    )

  private def ukRentARoomAllowancesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.allowances",
      controllers.ukrentaroom.allowances.routes.RRAllowancesStartController.onPageLoad(taxYear),
      {
        val sectionFinished = userAnswers.flatMap(_.get(RaRAllowancesCompletePage))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (
            userAnswers
              .flatMap(_.get(RaRElectricChargePointAllowanceForAnEVPage))
              .isDefined || userAnswers.flatMap(_.get(RaRCapitalAllowancesForACarPage)).isDefined
          ) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rent_a_room_allowances_link"
    )



  private def ukRentARoomAdjustmentsItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.adjustments",
      controllers.ukrentaroom.adjustments.routes.RaRAdjustmentsIntroController.onPageLoad(taxYear), {
        val sectionFinished = userAnswers.flatMap(_.get(RaRAdjustmentsCompletePage))

        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(RaRBalancingChargePage)).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rent_a_room_adjustments_link"
    )

}
