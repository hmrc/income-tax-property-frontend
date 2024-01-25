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

package pages.propertyrentals

import base.SpecBase
import models.UKPropertySelect
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.{SummaryPage, UKPropertyPage}
import viewmodels.summary.{TaskListItem, TaskListTag}

import java.time.LocalDate

class SummaryPageSpec extends SpecBase {

  "SummaryPageSpec createUkPropertyRows" - {
    val taxYear = LocalDate.now.getYear
    val cashOrAccurals = true
    val summaryItem = TaskListItem(
      "summary.about",
      controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "about_link"
    )
    val incomeListItem = TaskListItem(
      "summary.income",
      controllers.propertyrentals.routes.PropertyIncomeStartController.onPageLoad(taxYear),
      TaskListTag.InProgress,
      "income_link"
    )
    val expenseListItem = TaskListItem("summary.expenses",
      controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "expenses_link"
    )
    val propertyAllowances: TaskListItem = TaskListItem("summary.allowances",
      controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "allowances_link"
    )
    val structuresAndBuildingAllowance: TaskListItem = TaskListItem("summary.structuresAndBuildingAllowance",
      controllers.structuresbuildingallowance.routes.SummaryStartStructureBuildingAllowanceController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "structuresAndBuildingAllowance_link"
    )
    val adjustmentsListItem = TaskListItem("summary.adjustments",
      controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "adjustments_link"
    )
    val enhancedStructuresAndBuildingAllowance: TaskListItem = TaskListItem("summary.enhancedStructuresAndBuildingAllowance",
      controllers.routes.SummaryController.show(taxYear), //Todo: Replace when enhanced controller is implemented
      TaskListTag.NotStarted,
      "enhancedStructuresAndBuildingAllowance_link"
    )
    "return empty rows, given an empty user data" in {
      SummaryPage.createUkPropertyRows(Some(emptyUserAnswers), taxYear, cashOrAccurals).length should be(0)
    }

    "createUkPropertyRows return only one row when user has selected PropertyRentals but not selected ClaimPropertyIncomeAllowancePage" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers.set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      ).success.value

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals).length should be(1)
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals).head should be(summaryItem)

    }
    "should return all rows except expenses when ClaimPropertyIncomeAllowancePage exist in the user data" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers.set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      ).success.value.set(ClaimPropertyIncomeAllowancePage, true).success.value

      val res = Seq(summaryItem, incomeListItem, adjustmentsListItem)

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals).length should be(3)
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals) should be(res)
    }

    "should return all rows when ClaimPropertyIncomeAllowance is false and CashOrAccurals is true in the user data" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers.set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      ).success.value.set(ClaimPropertyIncomeAllowancePage, false).success.value

      val res = Seq(
        summaryItem,
        incomeListItem,
        expenseListItem,
        propertyAllowances,
        structuresAndBuildingAllowance,
        enhancedStructuresAndBuildingAllowance,
        adjustmentsListItem
      )

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals).length should be(7)
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals) should be(res)
    }
    "should return all rows except structuresAndBuildingAllowance when ClaimPropertyIncomeAllowance is false and CashOrAccurals is false in the user data" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers.set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      ).success.value.set(ClaimPropertyIncomeAllowancePage, false).success.value
      val cashOrAccurals = false

      val res = Seq(summaryItem, incomeListItem, expenseListItem, propertyAllowances, adjustmentsListItem)

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals).length should be(5)
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals) should be(res)
    }

  }
}
