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

package pages.foreign

import models.{NormalMode, UserAnswers}
import pages.foreign.adjustments.ForeignAdjustmentsCompletePage
import pages.foreign.allowances.ForeignAllowancesCompletePage
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import pages.foreign.income.ForeignIncomeSectionCompletePage
import pages.foreign.structurebuildingallowance.{ForeignClaimStructureBuildingAllowancePage, ForeignSbaCompletePage, ForeignStructureBuildingAllowanceGroup}
import play.api.mvc.Call
import service.ForeignCYADiversionService
import viewmodels.summary.{TaskListItem, TaskListTag}

case class ForeignPropertySummaryPage(
  taxYear: Int,
  startItems: Seq[TaskListItem],
  foreignPropertyItems: Map[String, Seq[TaskListItem]],
  foreignIncomeCountries: List[Country],
  userAnswers: Option[UserAnswers]
)

case class ForeignSummaryPage(foreignCYADiversionService: ForeignCYADiversionService) {

  def foreignPropertyAboutItems(taxYear: Int, userAnswers: Option[UserAnswers]): Seq[TaskListItem] = {
    val isCompleteSection = userAnswers.flatMap(_.get(ForeignSelectCountriesCompletePage))
    val taskListTag = isCompleteSection
      .map(haveYouFinished => if (haveYouFinished) TaskListTag.Completed else TaskListTag.InProgress)
      .getOrElse {
        if (isCompleteSection.isDefined) {
          TaskListTag.InProgress
        } else {
          TaskListTag.NotStarted
        }
      }

    Seq(
      TaskListItem(
        "foreign.selectCountry",
        foreignCYADiversionService
          .redirectCallToCYAIfFinished(taxYear, userAnswers, ForeignCYADiversionService.SELECT_COUNTRY, None) {
            controllers.foreign.routes.ForeignPropertyDetailsController.onPageLoad(taxYear)
          },
        taskListTag,
        "foreign_property_select_country"
      )
    )
  }

  def foreignPropertyItems(taxYear: Int, accrualsOrCash: Boolean, countryCode: String, userAnswers: Option[UserAnswers]): Seq[TaskListItem] = {
    val taskListTagForForeignTax =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignTaxSectionCompletePage(countryCode)).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    val taskListTagForIncome =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignIncomeSectionCompletePage(countryCode)).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    val taskListTagForExpenses =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignExpensesSectionCompletePage(countryCode)).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    val taskListTagForSba =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignSbaCompletePage(countryCode)).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    val taskListTagForAllowances =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignAllowancesCompletePage(countryCode)).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    val taskListTagForAdjustments = {
      val isAdjustmentsComplete = userAnswers.flatMap(_.get(ForeignAdjustmentsCompletePage(countryCode)))
      isAdjustmentsComplete
        .map { finishedYesOrNo =>
          if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
        }
        .getOrElse {
          val isPIA = userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowanceOrExpensesPage)).getOrElse(false)
          if (isPIA) {
            if (taskListTagForIncome == TaskListTag.Completed) {
              TaskListTag.NotStarted
            } else {
              TaskListTag.CanNotStart
            }
          } else {
            TaskListTag.NotStarted
          }
        }
    }

    val foreignTaxTaskList = TaskListItem(
      "foreign.tax",
      foreignCYADiversionService.redirectCallToCYAIfFinished(
        taxYear,
        userAnswers,
        ForeignCYADiversionService.FOREIGN_TAX,
        Some(countryCode)
      ) {
        controllers.foreign.routes.ForeignIncomeTaxController.onPageLoad(taxYear, countryCode, NormalMode)
      },
      taskListTagForForeignTax,
      s"foreign_property_income_tax_$countryCode"
    )
    val foreignIncomeTaskList = TaskListItem(
      "foreign.income",
      foreignCYADiversionService.redirectCallToCYAIfFinished(
        taxYear,
        userAnswers,
        ForeignCYADiversionService.INCOME,
        Some(countryCode)
      ) {
        controllers.foreign.income.routes.ForeignPropertyIncomeStartController.onPageLoad(taxYear, countryCode)
      },
      taskListTagForIncome,
      s"foreign_property_income_$countryCode"
    )

    val claimingAdjustmentsTaskList = TaskListItem(
      "summary.adjustments",
      foreignCYADiversionService.redirectCallToCYAIfFinished(
        taxYear,
        userAnswers,
        ForeignCYADiversionService.ADJUSTMENTS,
        Some(countryCode)
      ) {
        controllers.foreign.adjustments.routes.ForeignAdjustmentsStartController
          .onPageLoad(taxYear, countryCode, isPIA = true)
      },
      taskListTagForAdjustments,
      s"foreign_property_adjustments_$countryCode"
    )

    val expensesTaskList = TaskListItem(
      "summary.expenses",
      foreignCYADiversionService.redirectCallToCYAIfFinished(
        taxYear,
        userAnswers,
        ForeignCYADiversionService.EXPENSES,
        Some(countryCode)
      ) {
        controllers.foreign.expenses.routes.ForeignPropertyExpensesStartController
          .onPageLoad(taxYear, countryCode)
      },
      taskListTagForExpenses,
      s"foreign_property_expenses_$countryCode"
    )

    val allowancesTaskList = TaskListItem(
      "summary.allowances",
      foreignCYADiversionService.redirectCallToCYAIfFinished(
        taxYear,
        userAnswers,
        ForeignCYADiversionService.ALLOWANCES,
        Some(countryCode)
      ) {
        controllers.foreign.allowances.routes.ForeignPropertyAllowancesStartController
          .onPageLoad(taxYear, countryCode)
      },
      taskListTagForAllowances,
      s"foreign_property_allowances_$countryCode"
    )

    val sbaTaskList =TaskListItem(
        "summary.structuresAndBuildingAllowance",
        getSbaRouteDestination(taxYear, countryCode, userAnswers, taskListTagForSba),
        taskListTagForSba,
        s"foreign_structure_and_building_allowance_$countryCode"
      )

    val nonClaimingAdjustmentsTaskList = TaskListItem(
      "summary.adjustments",
      foreignCYADiversionService.redirectCallToCYAIfFinished(
        taxYear,
        userAnswers,
        ForeignCYADiversionService.ADJUSTMENTS,
        Some(countryCode)
      ) {
        controllers.foreign.adjustments.routes.ForeignAdjustmentsStartController
          .onPageLoad(taxYear, countryCode, isPIA = false)
      },
      taskListTagForAdjustments,
      s"foreign_property_adjustments_$countryCode"
    )

    val isClaimingAllowances = userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowanceOrExpensesPage))
    isClaimingAllowances match {
      case Some(true)  => Seq(foreignTaxTaskList, foreignIncomeTaskList, claimingAdjustmentsTaskList)
      case Some(false) if accrualsOrCash => Seq(foreignTaxTaskList, foreignIncomeTaskList, expensesTaskList, allowancesTaskList) ++ Seq(sbaTaskList) ++ Seq(nonClaimingAdjustmentsTaskList)
      case Some(false) => Seq(foreignTaxTaskList, foreignIncomeTaskList, expensesTaskList, allowancesTaskList) ++ Seq(nonClaimingAdjustmentsTaskList)
      case None        => Seq(foreignTaxTaskList, foreignIncomeTaskList)
    }
  }

  private def getSbaRouteDestination(
    taxYear: Int,
    countryCode: String,
    userAnswers: Option[UserAnswers],
    taskListTag: TaskListTag.TaskListTag
  ): Call =
    taskListTag match {
      case TaskListTag.InProgress | TaskListTag.Completed =>
        val answers = userAnswers.get
        (
          answers.get(ForeignClaimStructureBuildingAllowancePage(countryCode)),
          answers.get(ForeignStructureBuildingAllowanceGroup(countryCode))
        ) match {
          case (Some(true), Some(sbaForm)) if sbaForm.nonEmpty =>
            controllers.foreign.structuresbuildingallowance.routes.ForeignStructureBuildingAllowanceClaimsController
              .onPageLoad(taxYear, countryCode)

          case (Some(false), _) =>
            controllers.foreign.structuresbuildingallowance.routes.ForeignClaimSbaCheckYourAnswersController
              .onPageLoad(taxYear, countryCode)

          case (_, _) =>
            controllers.foreign.structuresbuildingallowance.routes.ForeignClaimStructureBuildingAllowanceController
              .onPageLoad(taxYear, countryCode, NormalMode)
        }
      case _ =>
        controllers.foreign.structuresbuildingallowance.routes.ForeignClaimStructureBuildingAllowanceController
          .onPageLoad(taxYear, countryCode, NormalMode)
    }
}
