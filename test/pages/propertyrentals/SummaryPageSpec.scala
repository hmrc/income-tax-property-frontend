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
import models.TotalIncome.writes
import models.{ClaimExpensesOrRelief, NormalMode, RentARoom, Rentals, UKPropertySelect}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import pages.ukrentaroom.ClaimExpensesOrReliefPage
import pages.{SummaryPage, UKPropertyPage}
import viewmodels.summary.{TaskListItem, TaskListTag}

import java.time.LocalDate

class SummaryPageSpec extends SpecBase {

  "SummaryPageSpec createUkPropertyRows" - {
    val taxYear = LocalDate.now.getYear
    val cashOrAccruals = true
    val summaryItem = TaskListItem(
      "summary.about",
      controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
      TaskListTag.InProgress,
      "rentals_about_link"
    )
    val incomeListItem = TaskListItem(
      "summary.income",
      controllers.propertyrentals.income.routes.PropertyIncomeStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rentals_income_link"
    )
    val expenseListItem = TaskListItem(
      "summary.expenses",
      controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rentals_expenses_link"
    )
    val propertyAllowances: TaskListItem = TaskListItem(
      "summary.allowances",
      controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rentals_allowances_link"
    )
    val structuresAndBuildingAllowance: TaskListItem = TaskListItem(
      "summary.structuresAndBuildingAllowance",
      controllers.structuresbuildingallowance.routes.ClaimStructureBuildingAllowanceController
        .onPageLoad(taxYear, NormalMode),
      TaskListTag.NotStarted,
      "rentals_structures_and_building_allowance_link"
    )
    val adjustmentsListItem = TaskListItem(
      "summary.adjustments",
      controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rentals_adjustments_link"
    )
    val enhancedStructuresAndBuildingAllowance: TaskListItem = TaskListItem(
      "summary.enhancedStructuresAndBuildingAllowance",
      controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController.onPageLoad(taxYear, NormalMode),
      TaskListTag.NotStarted,
      "rentals_enhanced_structures_and_building_allowance_link"
    )
    "return empty rows, given an empty user data" in {
      SummaryPage.createUkPropertyRows(Some(emptyUserAnswers), taxYear, cashOrAccruals).length should be(0)
    }

    "createUkPropertyRows return only one row when user has selected PropertyRentals but not selected ClaimPropertyIncomeAllowancePage" in {
      val summaryItem = TaskListItem(
        "summary.about",
        controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
        TaskListTag.NotStarted,
        "rentals_about_link"
      )

      val userAnswersWithPropertyRentals = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
        )
        .success
        .value

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals).length should be(
        1
      )
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals).head should be(
        summaryItem
      )

    }
    "should return all rows except expenses when ClaimPropertyIncomeAllowancePage exist in the user data" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
        )
        .success
        .value
        .set(ClaimPropertyIncomeAllowancePage(Rentals), true)
        .success
        .value

      val res = Seq(summaryItem, incomeListItem, adjustmentsListItem)

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals).length should be(
        3
      )
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }

    "should return all rows when ClaimPropertyIncomeAllowance is false and CashOrAccurals is true in the user data" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
        )
        .success
        .value
        .set(ClaimPropertyIncomeAllowancePage(Rentals), false)
        .success
        .value

      val res = Seq(
        summaryItem,
        incomeListItem,
        expenseListItem,
        propertyAllowances,
        structuresAndBuildingAllowance,
        enhancedStructuresAndBuildingAllowance,
        adjustmentsListItem
      )

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals).length should be(
        7
      )
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }
    "should return all rows except structuresAndBuildingAllowance when ClaimPropertyIncomeAllowance is false and CashOrAccurals is false in the user data" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
        )
        .success
        .value
        .set(ClaimPropertyIncomeAllowancePage(Rentals), false)
        .success
        .value
      val cashOrAccurals = false

      val res = Seq(summaryItem, incomeListItem, expenseListItem, propertyAllowances, adjustmentsListItem)

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals).length should be(
        5
      )
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals) should be(res)
    }

  }
  "SummaryPageSpec createUkRentARoomRows" - {
    val taxYear = LocalDate.now.getYear
    val summaryAboutItem = TaskListItem(
      "summary.about",
      controllers.ukrentaroom.routes.JointlyLetController.onPageLoad(taxYear, NormalMode, RentARoom),
      TaskListTag.NotStarted,
      "rent_a_room_about_link"
    )
    val summaryExpensesItem = TaskListItem(
      "summary.expenses",
      controllers.ukrentaroom.expenses.routes.UkRentARoomExpensesIntroController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rent_a_room_expenses_link"
    )
    val summaryAllowancesItem = TaskListItem(
      "summary.allowances",
      controllers.ukrentaroom.allowances.routes.RRAllowancesStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rent_a_room_allowances_link"
    )
    val summaryAdjustmentsItem = TaskListItem(
      "summary.adjustments",
      controllers.ukrentaroom.adjustments.routes.RaRAdjustmentsIntroController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rent_a_room_adjustments_link"
    )
    "return empty rows, given an empty user data" in {
      SummaryPage.createUkRentARoomRows(Some(emptyUserAnswers), taxYear).length should be(0)
    }

    "createUkRentARoomRows return only one row when user has selected Rent a room" in {
      val userAnswersWithUkRentARoom = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.RentARoom)
        )
        .success
        .value

      // ToDo: Should be updated when expenses selection page ticket is merged.
      SummaryPage.createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear).length should be(1)
      SummaryPage.createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear) should be(
        Seq(summaryAboutItem)
      )

    }
    "createUkRentARoomRows return three rows when user has selected Rent a room and Rent a Room Relief" in {
      val userAnswersWithUkRentARoom = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.RentARoom)
        )
        .success
        .value
        .set(
          ClaimExpensesOrReliefPage(RentARoom),
          ClaimExpensesOrRelief(claimExpensesOrReliefYesNo = true, Some(12.34))
        )
        .success
        .value

      SummaryPage.createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear).length should be(1)
      SummaryPage.createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear) should be(
        Seq(summaryAboutItem)
      )

    }
    "createUkRentARoomRows return four rows when user has selected Rent a room and Expenses" in {
      val userAnswersWithUkRentARoom = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.RentARoom)
        )
        .success
        .value
        .set(ClaimExpensesOrReliefPage(RentARoom), ClaimExpensesOrRelief(false, Some(12.34)))
        .success
        .value

      // ToDo: Should be updated when expenses selection page ticket is merged.
      SummaryPage.createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear).length should be(4)
      SummaryPage.createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear) should be(
        Seq(summaryAboutItem, summaryExpensesItem, summaryAllowancesItem, summaryAdjustmentsItem)
      )

    }
  }

  "SummaryPageSpec createRentalsAndRentARoomRows" - {
    val taxYear = 2024
    val summaryAboutItem = TaskListItem(
      "summary.about",
      controllers.rentalsandrentaroom.routes.RentalsRentARoomStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rentals_and_rent_a_room_about_link"
    )
    val summaryExpensesItem = TaskListItem(
      "summary.income",
      controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rentals_and_rent_a_room_income_link"
    )

    "return empty rows, given an empty user data" in {
      SummaryPage.createRentalsAndRentARoomRows(Some(emptyUserAnswers), taxYear).length should be(0)
    }

    "createRentalsAndRentARoomRows return only one row when user has selected Rentals and Rent a room" in {
      val userAnswersWithRentalsAndRentARoom = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.PropertyRentals, UKPropertySelect.RentARoom)
        )
        .success
        .value

      SummaryPage.createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear).length should be(1)
      SummaryPage.createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear) should be(
        Seq(summaryAboutItem)
      )

    }

    "createRentalsAndRentARoomRows return two rows when user has selected Rentals And Rent a room and Completed about section" in {

      val summaryAboutItem = TaskListItem(
        "summary.about",
        controllers.rentalsandrentaroom.routes.RentalsRentARoomStartController.onPageLoad(taxYear),
        TaskListTag.Completed,
        "rentals_and_rent_a_room_about_link"
      )

      val userAnswersWithRentalsAndRentARoom = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.PropertyRentals, UKPropertySelect.RentARoom)
        )
        .success
        .value
        .set(RentalsRaRAboutCompletePage, true)
        .success
        .value

      SummaryPage.createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear).length should be(2)
      SummaryPage.createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear) should be(
        Seq(summaryAboutItem, summaryExpensesItem)
      )

    }
  }

  "SummaryPageSpec property start rows" - {
    val taxYear = 2024
    "return one item, given an empty user data" in {
      val item = TaskListItem(
        "summary.about",
        controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear),
        TaskListTag.NotStarted,
        "property_about_link"
      )
      SummaryPage.propertyAboutItems(Some(emptyUserAnswers), taxYear) should be(Seq(item))

    }

  }

}
