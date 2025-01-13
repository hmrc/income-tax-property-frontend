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
import models.{ClaimExpensesOrRelief, NormalMode, RentARoom, Rentals, RentalsRentARoom, UKPropertySelect, UserAnswers}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.adjustments.RentalsAdjustmentsCompletePage
import pages.propertyrentals.income.IncomeSectionFinishedPage
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import pages.ukrentaroom.ClaimExpensesOrReliefPage
import pages.{SummaryPage, UKPropertyPage}
import service.CYADiversionService
import viewmodels.summary.{TaskListItem, TaskListTag}

import java.time.LocalDate

class SummaryPageSpec extends SpecBase {
  private val cyaDiversionService = new CYADiversionService()
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
      controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear, Rentals),
      TaskListTag.NotStarted,
      "rentals_expenses_link"
    )
    val propertyAllowances: TaskListItem = TaskListItem(
      "summary.allowances",
      controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear, Rentals),
      TaskListTag.NotStarted,
      "rentals_allowances_link"
    )
    val structuresAndBuildingAllowance: TaskListItem = TaskListItem(
      "summary.structuresAndBuildingAllowance",
      controllers.structuresbuildingallowance.routes.ClaimStructureBuildingAllowanceController
        .onPageLoad(taxYear, NormalMode, Rentals),
      TaskListTag.NotStarted,
      "rentals_structures_and_building_allowance_link"
    )
    val adjustmentsListItem = TaskListItem(
      "summary.adjustments",
      controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, false),
      TaskListTag.NotStarted,
      "rentals_adjustments_link"
    )
    val enhancedStructuresAndBuildingAllowance: TaskListItem = TaskListItem(
      "summary.enhancedStructuresAndBuildingAllowance",
      controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController
        .onPageLoad(taxYear, NormalMode, Rentals),
      TaskListTag.NotStarted,
      "rentals_enhanced_structures_and_building_allowance_link"
    )
    "return empty rows, given an empty user data" in {
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(emptyUserAnswers), taxYear, cashOrAccruals)
        .length should be(0)
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

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        1
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .head should be(
        summaryItem
      )

    }
    "should return all rows except expenses when ClaimPropertyIncomeAllowancePage exist in the user data" in {

      val adjustmentsListItem = TaskListItem(
        "summary.adjustments",
        controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, true),
        TaskListTag.CanNotStart,
        "rentals_adjustments_link"
      )

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

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        3
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }

    "Pia == true incomeSectionFinishedPage == false adjustmentSectionFinished = None adjustment taskListTag should be CanNotStart" in {

      val adjustmentsListItem = TaskListItem(
        "summary.adjustments",
        controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, true),
        TaskListTag.CanNotStart,
        "rentals_adjustments_link"
      )

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
        .set(IncomeSectionFinishedPage, false)
        .success
        .value

      val res = Seq(summaryItem, incomeListItem.copy(taskListTag = TaskListTag.InProgress), adjustmentsListItem)

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        3
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }
    "Pia == true incomeSectionFinishedPage == None adjustmentSectionFinished = None adjustment taskListTag should be CanNotStart" in {

      val adjustmentsListItem = TaskListItem(
        "summary.adjustments",
        controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, true),
        TaskListTag.CanNotStart,
        "rentals_adjustments_link"
      )

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

      val res = Seq(summaryItem, incomeListItem.copy(taskListTag = TaskListTag.NotStarted), adjustmentsListItem)

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        3
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }
    "Pia == true incomeSectionFinishedPage == true adjustmentSectionFinished = None adjustment taskListTag should be NotStarted" in {

      val adjustmentsListItem = TaskListItem(
        "summary.adjustments",
        controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, true),
        TaskListTag.NotStarted,
        "rentals_adjustments_link"
      )

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
        .set(IncomeSectionFinishedPage, true)
        .success
        .value

      val res = Seq(
        summaryItem,
        incomeListItem.copy(
          taskListTag = TaskListTag.Completed,
          call = controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
        ),
        adjustmentsListItem
      )

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        3
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }
    "Pia == true incomeSectionFinishedPage == true adjustmentSectionFinished = false adjustment taskListTag should be InProgress" in {

      val adjustmentsListItem = TaskListItem(
        "summary.adjustments",
        controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, true),
        TaskListTag.InProgress,
        "rentals_adjustments_link"
      )

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
        .set(IncomeSectionFinishedPage, true)
        .success
        .value
        .set(RentalsAdjustmentsCompletePage, false)
        .success
        .value

      val res = Seq(
        summaryItem,
        incomeListItem.copy(
          taskListTag = TaskListTag.Completed,
          call = controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
        ),
        adjustmentsListItem
      )

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        3
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }
    "Pia == true incomeSectionFinishedPage == true adjustmentSectionFinished = true adjustment taskListTag should be Completed" in {

      val adjustmentsListItem = TaskListItem(
        "summary.adjustments",
        controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, true),
        TaskListTag.Completed,
        "rentals_adjustments_link"
      )

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
        .set(IncomeSectionFinishedPage, true)
        .success
        .value
        .set(RentalsAdjustmentsCompletePage, true)
        .success
        .value

      val res = Seq(
        summaryItem,
        incomeListItem.copy(
          taskListTag = TaskListTag.Completed,
          call = controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
        ),
        adjustmentsListItem.copy(call =
          controllers.adjustments.routes.AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
        )
      )

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        3
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }
    "Pia == false  adjustmentSectionFinished = None adjustment taskListTag should be NotStarted" in {

      val adjustmentsListItem = TaskListItem(
        "summary.adjustments",
        controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, false),
        TaskListTag.NotStarted,
        "rentals_adjustments_link"
      )

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

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        7
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }

    "Pia == false  adjustmentSectionFinished = true adjustment taskListTag should be Completed" in {

      val adjustmentsListItem = TaskListItem(
        "summary.adjustments",
        controllers.adjustments.routes.AdjustmentsCheckYourAnswersController.onPageLoad(taxYear),
        TaskListTag.Completed,
        "rentals_adjustments_link"
      )

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
        .set(RentalsAdjustmentsCompletePage, true)
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

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        7
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
    }
    "Pia == false  adjustmentSectionFinished = false adjustment taskListTag should be InProgress" in {

      val adjustmentsListItem = TaskListItem(
        "summary.adjustments",
        controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, false),
        TaskListTag.InProgress,
        "rentals_adjustments_link"
      )

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
        .set(RentalsAdjustmentsCompletePage, false)
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

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        7
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
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

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals)
        .length should be(
        7
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccruals) should be(res)
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

      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals)
        .length should be(
        5
      )
      SummaryPage(cyaDiversionService)
        .createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear, cashOrAccurals) should be(res)
    }

  }
  "SummaryPageSpec createUkRentARoomRows" - {
    val taxYear = LocalDate.now.getYear
    val summaryAboutItem = TaskListItem(
      "summary.about",
      controllers.ukrentaroom.routes.RentARoomStartController.onPageLoad(taxYear),
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
      SummaryPage(cyaDiversionService).createUkRentARoomRows(Some(emptyUserAnswers), taxYear).length should be(0)
    }

    "createUkRentARoomRows return only one row when user has selected Rent a room" in {
      val userAnswersWithUkRentARoom = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.RentARoom)
        )
        .success
        .value

      SummaryPage(cyaDiversionService)
        .createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear)
        .length should be(1)
      SummaryPage(cyaDiversionService).createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear) should be(
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

      SummaryPage(cyaDiversionService)
        .createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear)
        .length should be(1)
      SummaryPage(cyaDiversionService).createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear) should be(
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
        .set(
          ClaimExpensesOrReliefPage(RentARoom),
          ClaimExpensesOrRelief(claimExpensesOrReliefYesNo = false, Some(12.34))
        )
        .success
        .value

      SummaryPage(cyaDiversionService)
        .createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear)
        .length should be(4)
      SummaryPage(cyaDiversionService).createUkRentARoomRows(Some(userAnswersWithUkRentARoom), taxYear) should be(
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
    val summaryIncomeItem = TaskListItem(
      "summary.income",
      controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeStartController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "rentals_and_rent_a_room_income_link"
    )

    val summaryExpenseItem = TaskListItem(
      "summary.expenses",
      controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear, RentalsRentARoom),
      TaskListTag.NotStarted,
      "rentals_and_rent_a_room_expenses_link"
    )

    val summaryAllowancesItem = TaskListItem(
      "summary.allowances",
      controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear, RentalsRentARoom),
      TaskListTag.NotStarted,
      "rentals_and_rent_a_room_allowances_link"
    )

    val summaryAdjustmentsItem = TaskListItem(
      "summary.adjustments",
      controllers.rentalsandrentaroom.adjustments.routes.RentalsAndRentARoomAdjustmentsStartController
        .onPageLoad(taxYear, true),
      TaskListTag.CanNotStart,
      "rentals_and_rent_a_room_adjustments_link"
    )

    val summarySBAItem = TaskListItem(
      "summary.structuresAndBuildingAllowance",
      controllers.structuresbuildingallowance.routes.ClaimStructureBuildingAllowanceController
        .onPageLoad(taxYear, NormalMode, RentalsRentARoom),
      TaskListTag.NotStarted,
      "rentals_and_rent_a_room_structures_and_building_allowance_link"
    )

    val summaryESBAItem = TaskListItem(
      "summary.enhancedStructuresAndBuildingAllowance",
      controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController
        .onPageLoad(taxYear, NormalMode, RentalsRentARoom),
      TaskListTag.NotStarted,
      "rentals_and_rent_a_room_enhanced_structures_and_building_allowance_link"
    )

    "return empty rows, given an empty user data" in {
      SummaryPage(cyaDiversionService)
        .createRentalsAndRentARoomRows(Some(emptyUserAnswers), taxYear, accrualsOrCash = true)
        .length should be(0)
    }

    "createRentalsAndRentARoomRows return only one row when user has selected Rentals and Rent a room" in {
      val userAnswersWithRentalsAndRentARoom = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.PropertyRentals, UKPropertySelect.RentARoom)
        )
        .success
        .value

      SummaryPage(cyaDiversionService)
        .createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear, accrualsOrCash = true)
        .length should be(1)
      SummaryPage(cyaDiversionService).createRentalsAndRentARoomRows(
        Some(userAnswersWithRentalsAndRentARoom),
        taxYear,
        accrualsOrCash = true
      ) should be(
        Seq(summaryAboutItem)
      )

    }

    "createRentalsAndRentARoomRows return three rows when user has selected Rentals And Rent a room and Completed about section" in {

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
        .set(ClaimPropertyIncomeAllowancePage(RentalsRentARoom), true)
        .success
        .value
        .set(ClaimExpensesOrReliefPage(RentalsRentARoom), ClaimExpensesOrRelief(false, None))
        .success
        .value

      SummaryPage(cyaDiversionService)
        .createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear, accrualsOrCash = true)
        .length should be(5)
      SummaryPage(cyaDiversionService).createRentalsAndRentARoomRows(
        Some(userAnswersWithRentalsAndRentARoom),
        taxYear,
        accrualsOrCash = true
      ) should be(
        Seq(
          summaryAboutItem.copy(
            taskListTag = TaskListTag.Completed,
            call = controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
          ),
          summaryIncomeItem,
          summaryExpenseItem,
          summaryAllowancesItem,
          summaryAdjustmentsItem
        )
      )

    }

    def createTestUserAnswersForRentalsRaRAbout(
      relief: ClaimExpensesOrRelief,
      pia: Boolean
    ): UserAnswers = emptyUserAnswers
      .set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals, UKPropertySelect.RentARoom)
      )
      .success
      .value
      .set(RentalsRaRAboutCompletePage, true)
      .success
      .value
      .set(ClaimExpensesOrReliefPage(RentalsRentARoom), relief)
      .success
      .value
      .set(ClaimPropertyIncomeAllowancePage(RentalsRentARoom), pia)
      .success
      .value

    "rentARoomRelief == true and pia == true and accrual == true" in {

      val userAnswersWithRentalsAndRentARoom =
        createTestUserAnswersForRentalsRaRAbout(ClaimExpensesOrRelief(true, Some(100)), true)

      SummaryPage(cyaDiversionService)
        .createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear, accrualsOrCash = true)
        .length should be(3)
      SummaryPage(cyaDiversionService).createRentalsAndRentARoomRows(
        Some(userAnswersWithRentalsAndRentARoom),
        taxYear,
        accrualsOrCash = true
      ) should be(
        Seq(
          summaryAboutItem.copy(
            taskListTag = TaskListTag.Completed,
            call = controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
          ),
          summaryIncomeItem,
          summaryAdjustmentsItem
        )
      )
    }

    "rentARoomRelief == false and pia == true and accrual == true" in {

      val userAnswersWithRentalsAndRentARoom =
        createTestUserAnswersForRentalsRaRAbout(ClaimExpensesOrRelief(false, None), true)

      SummaryPage(cyaDiversionService)
        .createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear, accrualsOrCash = true)
        .length should be(5)
      SummaryPage(cyaDiversionService).createRentalsAndRentARoomRows(
        Some(userAnswersWithRentalsAndRentARoom),
        taxYear,
        accrualsOrCash = true
      ) should be(
        Seq(
          summaryAboutItem.copy(
            taskListTag = TaskListTag.Completed,
            call = controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
          ),
          summaryIncomeItem,
          summaryExpenseItem,
          summaryAllowancesItem,
          summaryAdjustmentsItem
        )
      )
    }

    "rentARoomRelief == false and pia == false and accrual == true" in {

      val userAnswersWithRentalsAndRentARoom =
        createTestUserAnswersForRentalsRaRAbout(ClaimExpensesOrRelief(false, None), false)

      SummaryPage(cyaDiversionService)
        .createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear, accrualsOrCash = true)
        .length should be(7)
      SummaryPage(cyaDiversionService).createRentalsAndRentARoomRows(
        Some(userAnswersWithRentalsAndRentARoom),
        taxYear,
        accrualsOrCash = true
      ) should be(
        Seq(
          summaryAboutItem.copy(
            taskListTag = TaskListTag.Completed,
            call = controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
          ),
          summaryIncomeItem,
          summaryExpenseItem,
          summaryAllowancesItem,
          summarySBAItem,
          summaryESBAItem,
          summaryAdjustmentsItem.copy(
            call = controllers.rentalsandrentaroom.adjustments.routes.RentalsAndRentARoomAdjustmentsStartController
              .onPageLoad(taxYear, false),
            taskListTag = TaskListTag.NotStarted
          )
        )
      )
    }

    "rentARoomRelief == false and pia == true and accrual == false" in {

      val userAnswersWithRentalsAndRentARoom =
        createTestUserAnswersForRentalsRaRAbout(ClaimExpensesOrRelief(false, None), true)

      SummaryPage(cyaDiversionService)
        .createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear, accrualsOrCash = false)
        .length should be(5)
      SummaryPage(cyaDiversionService).createRentalsAndRentARoomRows(
        Some(userAnswersWithRentalsAndRentARoom),
        taxYear,
        accrualsOrCash = true
      ) should be(
        Seq(
          summaryAboutItem.copy(
            taskListTag = TaskListTag.Completed,
            call = controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
          ),
          summaryIncomeItem,
          summaryExpenseItem,
          summaryAllowancesItem,
          summaryAdjustmentsItem
        )
      )
    }

    "createRentalsAndRentARoomRows return five rows when user has selected claim expenses" in {

      val summaryAboutItem = TaskListItem(
        "summary.about",
        call = controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear),
        TaskListTag.Completed,
        "rentals_and_rent_a_room_about_link"
      )

      val rentalsRaRSummaryAdjustmentsItem = TaskListItem(
        "summary.adjustments",
        controllers.rentalsandrentaroom.adjustments.routes.RentalsAndRentARoomAdjustmentsStartController
          .onPageLoad(taxYear, false),
        TaskListTag.NotStarted,
        "rentals_and_rent_a_room_adjustments_link"
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
        .set(ClaimPropertyIncomeAllowancePage(RentalsRentARoom), false)
        .success
        .value
        .set(ClaimExpensesOrReliefPage(RentalsRentARoom), ClaimExpensesOrRelief(false, None))
        .success
        .value

      SummaryPage(cyaDiversionService)
        .createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear, accrualsOrCash = false)
        .length should be(5)
      SummaryPage(cyaDiversionService).createRentalsAndRentARoomRows(
        Some(userAnswersWithRentalsAndRentARoom),
        taxYear,
        accrualsOrCash = false
      ) should be(
        Seq(
          summaryAboutItem,
          summaryIncomeItem,
          summaryExpenseItem,
          summaryAllowancesItem,
          rentalsRaRSummaryAdjustmentsItem
        )
      )
    }

    "createRentalsAndRentARoomRows return seven rows when user has selected claim expenses for cash basis" in {

      val summaryAboutItem = TaskListItem(
        "summary.about",
        call = controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear),
        TaskListTag.Completed,
        "rentals_and_rent_a_room_about_link"
      )

      val rentalsRaRSummaryAdjustmentsItem = TaskListItem(
        "summary.adjustments",
        controllers.rentalsandrentaroom.adjustments.routes.RentalsAndRentARoomAdjustmentsStartController
          .onPageLoad(taxYear, false),
        TaskListTag.NotStarted,
        "rentals_and_rent_a_room_adjustments_link"
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
        .set(ClaimPropertyIncomeAllowancePage(RentalsRentARoom), false)
        .success
        .value
        .set(ClaimExpensesOrReliefPage(RentalsRentARoom), ClaimExpensesOrRelief(false, None))
        .success
        .value

      SummaryPage(cyaDiversionService)
        .createRentalsAndRentARoomRows(Some(userAnswersWithRentalsAndRentARoom), taxYear, accrualsOrCash = true)
        .length should be(7)
      SummaryPage(cyaDiversionService).createRentalsAndRentARoomRows(
        Some(userAnswersWithRentalsAndRentARoom),
        taxYear,
        accrualsOrCash = true
      ) should be(
        Seq(
          summaryAboutItem,
          summaryIncomeItem,
          summaryExpenseItem,
          summaryAllowancesItem,
          summarySBAItem,
          summaryESBAItem,
          rentalsRaRSummaryAdjustmentsItem
        )
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
      SummaryPage(cyaDiversionService).propertyAboutItems(Some(emptyUserAnswers), taxYear) should be(Seq(item))

    }

  }

}
