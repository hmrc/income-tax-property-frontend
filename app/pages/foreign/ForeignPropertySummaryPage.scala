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

import models.{CheckMode, NormalMode, UserAnswers}
import pages.foreign.allowances.ForeignAllowancesCompletePage
import pages.foreign.adjustments.ForeignAdjustmentsCompletePage
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import pages.foreign.income.ForeignIncomeSectionCompletePage
import pages.foreign.structurebuildingallowance.ForeignSbaCompletePage
import viewmodels.summary.{TaskListItem, TaskListTag}

case class ForeignPropertySummaryPage(
  taxYear: Int,
  startItems: Seq[TaskListItem],
  foreignIncomeCountries: List[Country],
  userAnswers: Option[UserAnswers]
)

object ForeignPropertySummaryPage {

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
        controllers.foreign.routes.ForeignPropertyDetailsController.onPageLoad(taxYear),
        taskListTag,
        "foreign_property_select_country"
      )
    )
  }

  def foreignPropertyItems(taxYear: Int, countryCode: String, userAnswers: Option[UserAnswers]): Seq[TaskListItem] = {
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
    val taskListTagForAdjustments =
      userAnswers
        .flatMap { answers =>
          answers.get(ForeignAdjustmentsCompletePage(countryCode)).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted)
    val taskList = {
      Seq(
        TaskListItem(
          "foreign.tax",
          controllers.foreign.routes.ForeignIncomeTaxController.onPageLoad(taxYear, countryCode, NormalMode),
          taskListTagForForeignTax,
          "foreign_property_income_tax"
        ),
        TaskListItem(
          "foreign.income",
          controllers.foreign.income.routes.ForeignPropertyIncomeStartController.onPageLoad(taxYear, countryCode),
          taskListTagForIncome,
          s"foreign_property_income_$countryCode"
        )
      )
    }
    val isClaimingAllowances = userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowanceOrExpensesPage))
    isClaimingAllowances match {
      case Some(true) => taskList.appendedAll(
        Seq(
          TaskListItem(
            "summary.adjustments",
            controllers.foreign.adjustments.routes.ForeignAdjustmentsStartController.onPageLoad(taxYear, countryCode, isClaimingAllowances.getOrElse(true)),
            taskListTagForAdjustments,
            s"foreign_property_adjustments_$countryCode"
          )
        )
      )
      case Some(false) => taskList.appendedAll(
        Seq(
          TaskListItem(
            "summary.adjustments",
            controllers.foreign.adjustments.routes.ForeignAdjustmentsStartController.onPageLoad(taxYear, countryCode, isClaimingAllowances.getOrElse(false)),
            taskListTagForAllowances,
            s"foreign_property_adjustments_$countryCode"
          ),
          TaskListItem(
            "summary.allowances",
            controllers.foreign.allowances.routes.ForeignPropertyAllowancesStartController.onPageLoad(taxYear, countryCode),
            taskListTagForAllowances,
            s"foreign_property_allowances_$countryCode"
          ),
          TaskListItem(
            "summary.expenses",
            controllers.foreign.expenses.routes.ForeignPropertyExpensesStartController.onPageLoad(taxYear, countryCode),
            taskListTagForExpenses,
            s"foreign_property_expenses_$countryCode"
          ),
          TaskListItem(
            "summary.structuresAndBuildingAllowance",
            controllers.foreign.structuresbuildingallowance.routes.ForeignClaimStructureBuildingAllowanceController.onPageLoad(taxYear, countryCode, CheckMode),
            taskListTagForSba,
            s"foreign_structure_and_building_allowance_$countryCode"
          )
        )
      )
      case None => taskList
    }
  }
}
