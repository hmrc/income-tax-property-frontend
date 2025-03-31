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

package pages.foreignincome

import models.UserAnswers
import service.ForeignIncomeCYADiversionService
import viewmodels.summary.{TaskListItem, TaskListTag}

case class ForeignIncomeSummaryViewModel(
  taxYear: Int,
  foreignIncomeItems: Seq[TaskListItem],
  userAnswers: Option[UserAnswers]
)

object ForeignIncomeSummaryPage {
  def apply(
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    foreignIncomeCYADiversionService: ForeignIncomeCYADiversionService
  ): ForeignIncomeSummaryViewModel = {
    val taskListItems = Seq(
      TaskListItem(
        "foreignIncome.dividends",
        foreignIncomeCYADiversionService
          .redirectCallToCYAIfFinished(taxYear, userAnswers, ForeignIncomeCYADiversionService.DIVIDENDS) {
            controllers.foreignincome.dividends.routes.ForeignDividendsStartController.onPageLoad(taxYear)
          },
        TaskListTag.NotStarted,
        "foreign_income_dividends"
      ),
      TaskListItem(
        "foreignIncome.interest",
        foreignIncomeCYADiversionService
          .redirectCallToCYAIfFinished(taxYear, userAnswers, ForeignIncomeCYADiversionService.INTEREST) {
            controllers.routes.IndexController.onPageLoad
          },
        TaskListTag.NotStarted,
        "foreign_income_interest"
      )
    )
    ForeignIncomeSummaryViewModel(
      taxYear = taxYear,
      foreignIncomeItems = taskListItems,
      userAnswers = userAnswers
    )
  }
}