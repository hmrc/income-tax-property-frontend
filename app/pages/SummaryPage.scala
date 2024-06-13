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
import pages.enhancedstructuresbuildingallowance.EsbaQualifyingDatePage
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import pages.structurebuildingallowance.StructureBuildingQualifyingDatePage
import pages.ukrentaroom.{AboutSectionCompletePage, ClaimExpensesOrRRRPage, UkRentARoomJointlyLetPage}
import viewmodels.summary.{TaskListItem, TaskListTag}

case object SummaryPage {
  def createUkPropertyRows(
    userAnswers: Option[UserAnswers],
    taxYear: Int,
    cashOrAccruals: Boolean
  ): Seq[TaskListItem] = {
    val propertyRentalsAbout: TaskListItem = propertyAboutItem(userAnswers, taxYear)
    val propertyRentalsIncome: TaskListItem = propertyRentalsIncomeItem(userAnswers, taxYear)
    val propertyRentalsExpenses: TaskListItem = propertyRentalsExpensesItem(userAnswers, taxYear)
    val propertyAllowances: TaskListItem = propertyAllowancesItem(taxYear)
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

    val claimRentARoomRelief = userAnswers.flatMap(_.get(ClaimExpensesOrRRRPage)).map(_.claimExpensesOrRRR)
    // ToDo: Should be updated when expenses selection page ticket is merged.
    if (isRentARoomSelected) {
      claimRentARoomRelief
        .collect {
          case true  => Seq(ukRentARoomAbout, ukRentARoomExpenses, ukRentARoomAllowances)
          case false => Seq(ukRentARoomAbout, ukRentARoomExpenses, ukRentARoomAllowances, ukRentARoomAdjustments)
        }
        .getOrElse(Seq(ukRentARoomAbout))
    } else {
      Seq.empty[TaskListItem]
    }
  }

  private def rentalsEsbaItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.enhancedStructuresAndBuildingAllowance",
      controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController.onPageLoad(taxYear, NormalMode),
      if (userAnswers.flatMap(_.get(EsbaQualifyingDatePage(0))).isDefined) {
        TaskListTag.InProgress
      } else {
        TaskListTag.NotStarted
      },
      "enhancedStructuresAndBuildingAllowance_link"
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
      "adjustments_link"
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
      "structuresAndBuildingAllowance_link"
    )

  private def propertyAllowancesItem(taxYear: Int) =
    TaskListItem(
      "summary.allowances",
      controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "allowances_link"
    )

  private def propertyRentalsExpensesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.expenses",
      controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear),
      if (userAnswers.flatMap(_.get(ConsolidatedExpensesPage)).isDefined) {
        TaskListTag.InProgress
      } else {
        TaskListTag.NotStarted
      },
      "expenses_link"
    )

  private def propertyRentalsIncomeItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.income",
      controllers.propertyrentals.income.routes.PropertyIncomeStartController.onPageLoad(taxYear),
      if (userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage)).isDefined) {
        TaskListTag.InProgress
      } else {
        TaskListTag.NotStarted
      },
      "income_link"
    )

  private def propertyAboutItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.about",
      controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
      if (userAnswers.flatMap(_.get(TotalIncomePage)).isDefined) TaskListTag.InProgress else TaskListTag.NotStarted,
      "about_link"
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
      "about_link"
    )

  private def ukRentARoomExpensesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.expenses",
      controllers.ukrentaroom.expenses.routes.UkRentARoomExpensesIntroController.onPageLoad(taxYear), {

        val sectionFinished = userAnswers.flatMap(_.get(ExpensesRRSectionCompletePage))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(UkRentARoomJointlyLetPage)).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "expenses_link"
    )

  private def ukRentARoomAllowancesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.allowances",
      controllers.ukrentaroom.allowances.routes.RRAllowancesStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "allowances_link"
    )

  private def ukRentARoomAdjustmentsItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.adjustments",
      controllers.ukrentaroom.adjustments.routes.RaRAdjustmentsIntroController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "adjustments_link"
    )

}
