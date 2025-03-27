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
    val taskList =
      Seq(
        foreignTaxItem(taxYear, countryCode, userAnswers),
        foreignIncomeItem(taxYear, countryCode, userAnswers)
      )

    val isClaimingAllowances = userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowanceOrExpensesPage))
    isClaimingAllowances match {
      case Some(true) => taskList ++ Seq(
        foreignAdjustmentsItem(taxYear, countryCode, isClaimPIA = true, userAnswers)
      )
      case Some(false) if accrualsOrCash => taskList ++ Seq(
        foreignExpensesItem(taxYear, countryCode, userAnswers),
        foreignAllowancesItem(taxYear, countryCode, userAnswers),
        foreignSBAItem(taxYear, countryCode, userAnswers),
        foreignAdjustmentsItem(taxYear, countryCode, isClaimPIA = false, userAnswers)
      )
      case Some(false) =>
        taskList ++ Seq(
          foreignExpensesItem(taxYear, countryCode, userAnswers),
          foreignAllowancesItem(taxYear, countryCode, userAnswers),
          foreignAdjustmentsItem(taxYear, countryCode, isClaimPIA = false, userAnswers))
      case None => taskList
    }
  }

  def foreignTaxItem(taxYear: Int, countryCode: String, userAnswers: Option[UserAnswers]): TaskListItem = {
    val taskListTagForForeignTax =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignTaxSectionCompletePage(countryCode)).map { isFinished =>
            if (isFinished) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    TaskListItem(
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
  }

  def foreignIncomeItem(taxYear: Int, countryCode: String, userAnswers: Option[UserAnswers]): TaskListItem = {
    val taskListTagForIncome =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignIncomeSectionCompletePage(countryCode)).map { isFinished =>
            if (isFinished) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    TaskListItem(
      "foreign.income",
      foreignCYADiversionService
        .redirectCallToCYAIfFinished(taxYear, userAnswers, ForeignCYADiversionService.INCOME, Some(countryCode)) {
          controllers.foreign.income.routes.ForeignPropertyIncomeStartController.onPageLoad(taxYear, countryCode)
        },
      taskListTagForIncome,
      s"foreign_property_income_$countryCode"
    )
  }

  def foreignExpensesItem(taxYear: Int, countryCode: String, userAnswers: Option[UserAnswers]): TaskListItem = {
    val taskListTagForExpenses =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignExpensesSectionCompletePage(countryCode)).map { isFinished =>
            if (isFinished) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    TaskListItem(
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
  }

  def foreignAllowancesItem(taxYear: Int, countryCode: String, userAnswers: Option[UserAnswers]): TaskListItem = {
    val taskListTagForAllowances =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignAllowancesCompletePage(countryCode)).map { isFinished =>
            if (isFinished) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    TaskListItem(
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
  }

  def foreignSBAItem(taxYear: Int, countryCode: String, userAnswers: Option[UserAnswers]): TaskListItem = {
    val taskListTagForSba =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignSbaCompletePage(countryCode)).map { isFinished =>
            if (isFinished) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)

    TaskListItem(
      "summary.structuresAndBuildingAllowance",
      getSbaRouteDestination(taxYear, countryCode, userAnswers, taskListTagForSba),
      taskListTagForSba,
      s"foreign_structure_and_building_allowance_$countryCode"
    )
  }

  def foreignAdjustmentsItem(
    taxYear: Int,
    countryCode: String,
    isClaimPIA: Boolean,
    userAnswers: Option[UserAnswers],
    isUkAndForeignJourney: Boolean = false
  ): TaskListItem = {
    val taskListTagForAdjustments = {
      val isAdjustmentsComplete = userAnswers.flatMap(_.get(ForeignAdjustmentsCompletePage(countryCode)))
      isAdjustmentsComplete
        .map { isFinished =>
          if (isFinished) TaskListTag.Completed else TaskListTag.InProgress
        }
        .getOrElse {
          val isPIA = userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowanceOrExpensesPage)).getOrElse(false)
          (isUkAndForeignJourney, isPIA) match {
            case (false, true) =>
              val isIncomeSectionCompleted =
                userAnswers.flatMap(_.get(ForeignIncomeSectionCompletePage(countryCode))).contains(true)
              if (isIncomeSectionCompleted) TaskListTag.NotStarted else TaskListTag.CanNotStart
            case (_, _) => TaskListTag.NotStarted
          }
        }
    }
    TaskListItem(
      "summary.adjustments",
      foreignCYADiversionService.redirectCallToCYAIfFinished(
        taxYear,
        userAnswers,
        ForeignCYADiversionService.ADJUSTMENTS,
        Some(countryCode)
      ) {
        controllers.foreign.adjustments.routes.ForeignAdjustmentsStartController
          .onPageLoad(taxYear, countryCode, isPIA = isClaimPIA)
      },
      taskListTagForAdjustments,
      s"foreign_property_adjustments_$countryCode"
    )
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
