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
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import pages.foreign.income.ForeignIncomeSectionCompletePage
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
      ),
      TaskListItem(
        "foreign.expenses",
        controllers.foreign.expenses.routes.ForeignPropertyExpensesStartController.onPageLoad(taxYear, countryCode),
        taskListTagForExpenses,
        s"foreign_property_expenses_$countryCode"
      )
    )
  }
}
