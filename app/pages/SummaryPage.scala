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

import models.{UKPropertySelect, UserAnswers}
import pages.adjustments.PrivateUseAdjustmentPage
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import viewmodels.summary.{TaskListItem, TaskListTag}

case object SummaryPage {
  def createUkPropertyRows(userAnswers: Option[UserAnswers], taxYear: Int): Seq[TaskListItem] = {
    val propertyRentalsAbout: TaskListItem = TaskListItem(
      "summary.about",
      controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
      if (userAnswers.flatMap(_.get(TotalIncomePage)).isDefined) TaskListTag.InProgress else TaskListTag.NotStarted,
      "about_link"
    )
    val propertyRentalsIncome: TaskListItem = TaskListItem(
      "summary.income",
      controllers.propertyrentals.routes.PropertyIncomeStartController.onPageLoad(taxYear),
      if (userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage)).isDefined) TaskListTag.InProgress else TaskListTag.NotStarted,
      "income_link"
    )
    val propertyRentalsExpenses: TaskListItem = TaskListItem("summary.expenses",
      controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear),
      if (userAnswers.flatMap(_.get(ConsolidatedExpensesPage)).isDefined) TaskListTag.InProgress else TaskListTag.NotStarted,
      "expenses_link"
    )
    val propertyAllowances: TaskListItem = TaskListItem("summary.allowances",
      controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "allowances_link"
    )
    val propertyRentalsAdjustments: TaskListItem = TaskListItem("summary.adjustments",
      controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear),
      if (userAnswers.flatMap(_.get(PrivateUseAdjustmentPage)).isDefined) TaskListTag.InProgress else TaskListTag.NotStarted,
      "adjustments_link"
    )

    val claimPropertyIncomeAllowance = userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage))
    val isPropertyRentalsSelected = userAnswers.exists(_.get(UKPropertyPage).exists(_.contains(UKPropertySelect.PropertyRentals)))

    claimPropertyIncomeAllowance.collect {
      case true => Seq(propertyRentalsAbout, propertyRentalsIncome, propertyRentalsAdjustments)
      case false => Seq(propertyRentalsAbout, propertyRentalsIncome, propertyRentalsExpenses, propertyAllowances, propertyRentalsAdjustments)
    }.getOrElse {
      if (isPropertyRentalsSelected) Seq(propertyRentalsAbout) else Seq.empty[TaskListItem]
    }
  }
}
