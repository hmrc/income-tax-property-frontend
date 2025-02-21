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

import models.{ClaimExpensesOrRelief, NormalMode, PropertyType, RentARoom, Rentals, RentalsRentARoom, UKPropertySelect, UserAnswers, UKProperty}
import pages.adjustments.RentalsAdjustmentsCompletePage
import pages.allowances.{AllowancesSectionFinishedPage, AnnualInvestmentAllowancePage, CapitalAllowancesForACarPage}
import pages.enhancedstructuresbuildingallowance.{ClaimEsbaPage, EsbaSectionFinishedPage}
import pages.propertyrentals.expenses.{ConsolidatedExpensesPage, ExpensesSectionFinishedPage, RentsRatesAndInsurancePage}
import pages.propertyrentals.income.IncomeSectionFinishedPage
import pages.propertyrentals.{AboutPropertyRentalsSectionFinishedPage, ClaimPropertyIncomeAllowancePage}
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import pages.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompletePage
import pages.rentalsandrentaroom.allowances.RentalsRaRAllowancesCompletePage
import pages.rentalsandrentaroom.expenses.RentalsRaRExpensesCompletePage
import pages.rentalsandrentaroom.income.RentalsRaRIncomeCompletePage
import pages.structurebuildingallowance.{ClaimStructureBuildingAllowancePage, SbaSectionFinishedPage}
import pages.ukrentaroom.adjustments.{RaRAdjustmentsCompletePage, RaRBalancingChargePage}
import pages.ukrentaroom.allowances.{RaRAllowancesCompletePage, RaRCapitalAllowancesForACarPage, RaRElectricChargePointAllowanceForAnEVPage}
import pages.ukrentaroom.expenses.{ConsolidatedExpensesRRPage, ExpensesRRSectionCompletePage, RentsRatesAndInsuranceRRPage}
import pages.ukrentaroom.{AboutSectionCompletePage, ClaimExpensesOrReliefPage, JointlyLetPage}
import play.api.mvc.Call
import service.CYADiversionService
import viewmodels.summary.{TaskListItem, TaskListTag}

case class UKPropertySummaryPage(
  taxYear: Int,
  startItems: Seq[TaskListItem],
  rentalsRows: Seq[TaskListItem],
  rentARoomRows: Seq[TaskListItem],
  combinedItems: Seq[TaskListItem]
)

case class SummaryPage(cyaDiversionService: CYADiversionService) {

  def createUkPropertyRows(
    userAnswers: Option[UserAnswers],
    taxYear: Int,
    accrualsOrCash: Boolean
  ): Seq[TaskListItem] = {
    val propertyRentalsAbout: TaskListItem = propertyRentalsAboutItem(userAnswers, taxYear)
    val propertyRentalsIncome: TaskListItem = propertyRentalsIncomeItem(userAnswers, taxYear)
    val propertyRentalsExpenses: TaskListItem = propertyRentalsExpensesItem(userAnswers, taxYear)
    val propertyAllowances: TaskListItem = propertyAllowancesItem(taxYear, userAnswers)
    val structuresAndBuildingAllowance: TaskListItem = structuresAndBuildingAllowanceItem(userAnswers, taxYear)
    val propertyRentalsAdjustments: TaskListItem = propertyRentalsAdjustmentsItem(userAnswers, taxYear)
    val enhancedStructuresAndBuildingAllowance: TaskListItem = rentalsEsbaItem(userAnswers, taxYear)

    val claimPropertyIncomeAllowance = userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage(Rentals)))
    val isRentARoomSelected = isSelected(userAnswers, UKPropertySelect.RentARoom)
    val isPropertyRentalsSelected = isSelected(userAnswers, UKPropertySelect.PropertyRentals)

    if (isPropertyRentalsSelected && !isRentARoomSelected) {
      claimPropertyIncomeAllowance
        .collect {
          case true => Seq(propertyRentalsAbout, propertyRentalsIncome, propertyRentalsAdjustments)
          case false if accrualsOrCash =>
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
    val isRentARoomSelected = isSelected(userAnswers, UKPropertySelect.RentARoom)
    val isPropertyRentalsSelected = isSelected(userAnswers, UKPropertySelect.PropertyRentals)

    val claimRentARoomRelief =
      userAnswers.flatMap(_.get(ClaimExpensesOrReliefPage(RentARoom))).map(_.claimExpensesOrReliefYesNo)
    if (isRentARoomSelected && !isPropertyRentalsSelected) {
      claimRentARoomRelief
        .collect {
          case true  => Seq(ukRentARoomAbout)
          case false => Seq(ukRentARoomAbout, ukRentARoomExpenses, ukRentARoomAllowances, ukRentARoomAdjustments)
        }
        .getOrElse(Seq(ukRentARoomAbout))
    } else {
      Seq.empty[TaskListItem]
    }
  }

  def createRentalsAndRentARoomRows(
    userAnswers: Option[UserAnswers],
    taxYear: Int,
    accrualsOrCash: Boolean
  ): Seq[TaskListItem] = {
    val isRentARoomSelected = isSelected(userAnswers, UKPropertySelect.RentARoom)
    val isPropertyRentalsSelected = isSelected(userAnswers, UKPropertySelect.PropertyRentals)

    if (isRentARoomSelected && isPropertyRentalsSelected) {
      createTaskListForCombinedJourney(
        userAnswers.flatMap(_.get(ClaimExpensesOrReliefPage(RentalsRentARoom))),
        userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom))),
        userAnswers,
        taxYear,
        accrualsOrCash
      )
    } else {
      Seq.empty[TaskListItem]
    }
  }

  private def createTaskListForCombinedJourney(
    claimExpensesOrRelief: Option[ClaimExpensesOrRelief],
    claimPropertyIncomeAllowance: Option[Boolean],
    userAnswers: Option[UserAnswers],
    taxYear: Int,
    accrualsOrCash: Boolean
  ): Seq[TaskListItem] =
    (claimExpensesOrRelief, claimPropertyIncomeAllowance, accrualsOrCash) match {
      case (None, None, _) | (None, Some(_), _) | (Some(_), None, _) =>
        Seq(rentalsAndRaRAboutItem(taxYear, userAnswers))
      // Accounting method == accruals
      case (Some(relief), Some(pia), true) =>
        createTaskListForAccruals(userAnswers, taxYear, relief, pia)
      // Accounting method == cash
      case (Some(relief), Some(pia), false) =>
        createTaskListForCash(userAnswers, taxYear, relief, pia)
    }

  private def createTaskListForCash(
    userAnswers: Option[UserAnswers],
    taxYear: Int,
    relief: ClaimExpensesOrRelief,
    pia: Boolean
  ) =
    (relief.claimExpensesOrReliefYesNo, pia) match {
      // RR01 - Rent a Room Relief (true) and Yes, claim property income allowance (true)
      case (true, true) =>
        Seq(
          rentalsAndRaRAboutItem(taxYear, userAnswers),
          rentalsAndRaRIncomeItem(taxYear, userAnswers),
          rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
        )
      // RR02 - Rent a Room Relief (false) i.e. Expenses and Yes, claim property income allowance (true)
      case (false, true) =>
        Seq(
          rentalsAndRaRAboutItem(taxYear, userAnswers),
          rentalsAndRaRIncomeItem(taxYear, userAnswers),
          rentalsAndRaRExpensesItem(taxYear, userAnswers),
          rentalsAndRaRAllowancesItem(taxYear, userAnswers),
          rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
        )
      // RR03 - Rent a Room Relief (false) i.e. Expenses and No, claim expenses (false)
      // RR04 - Rent a Room Relief (true) i.e. Expenses and Yes, claim property income allowance (true)
      case (false, false) | (true, false) =>
        Seq(
          rentalsAndRaRAboutItem(taxYear, userAnswers),
          rentalsAndRaRIncomeItem(taxYear, userAnswers),
          rentalsAndRaRExpensesItem(taxYear, userAnswers),
          rentalsAndRaRAllowancesItem(taxYear, userAnswers),
          rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
        )
    }

  private def createTaskListForAccruals(
    userAnswers: Option[UserAnswers],
    taxYear: Int,
    relief: ClaimExpensesOrRelief,
    pia: Boolean
  ) =
    (relief.claimExpensesOrReliefYesNo, pia) match {
      // RR01 - Rent a Room Relief (true) and Yes, claim property income allowance (true)
      case (true, true) =>
        Seq(
          rentalsAndRaRAboutItem(taxYear, userAnswers),
          rentalsAndRaRIncomeItem(taxYear, userAnswers),
          rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
        )
      // RR02 - Rent a Room Relief (false) i.e. Expenses and Yes, claim property income allowance (true)
      case (false, true) =>
        Seq(
          rentalsAndRaRAboutItem(taxYear, userAnswers),
          rentalsAndRaRIncomeItem(taxYear, userAnswers),
          rentalsAndRaRExpensesItem(taxYear, userAnswers),
          rentalsAndRaRAllowancesItem(taxYear, userAnswers),
          rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
        )
      // RR03 - Rent a Room Relief (false) i.e. Expenses and No, claim expenses (false)
      // RR04 - Rent a Room Relief (true) i.e. Expenses and Yes, claim property income allowance (true)
      case (false, false) | (true, false) =>
        Seq(
          rentalsAndRaRAboutItem(taxYear, userAnswers),
          rentalsAndRaRIncomeItem(taxYear, userAnswers),
          rentalsAndRaRExpensesItem(taxYear, userAnswers),
          rentalsAndRaRAllowancesItem(taxYear, userAnswers),
          rentalsAndRaRSBAItem(taxYear, userAnswers),
          rentalsAndRaRESBAItem(taxYear, userAnswers),
          rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
        )
    }

  def propertyAboutItems(userAnswers: Option[UserAnswers], taxYear: Int): Seq[TaskListItem] =
    Seq(
      TaskListItem(
        "summary.about",
        cyaDiversionService
          .redirectToCYAIfFinished[Call](taxYear, userAnswers, "about", UKProperty, NormalMode) {
            controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear)
          }(identity), {
          val sectionFinished = userAnswers.flatMap(_.get(AboutPropertyCompletePage))

          sectionFinished
            .map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress)
            .getOrElse {
              if (userAnswers.flatMap(_.get(ReportPropertyIncomePage)).isDefined) {
                TaskListTag.InProgress
              } else {
                TaskListTag.NotStarted
              }
            }
        },
        "property_about_link"
      )
    )

  private def rentalsEsbaItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.enhancedStructuresAndBuildingAllowance",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "esba", Rentals, NormalMode) {
          controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController
            .onPageLoad(taxYear, NormalMode, Rentals)
        }(identity),
      userAnswers
        .flatMap { answers =>
          answers.get(EsbaSectionFinishedPage(Rentals)).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_enhanced_structures_and_building_allowance_link"
    )

  private def propertyRentalsAdjustmentsItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.adjustments",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "adjustments", Rentals, NormalMode) {
          controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(
            taxYear,
            userAnswers
              .flatMap(_.get(ClaimPropertyIncomeAllowancePage(Rentals)))
              .getOrElse(false)
          )
        }(identity),
      getTaskListTagStatus(userAnswers, Rentals),
      "rentals_adjustments_link"
    )

  private def structuresAndBuildingAllowanceItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.structuresAndBuildingAllowance",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "sba", Rentals, NormalMode) {

          controllers.structuresbuildingallowance.routes.ClaimStructureBuildingAllowanceController
            .onPageLoad(taxYear, NormalMode, Rentals)
        }(identity),
      userAnswers
        .flatMap { answers =>
          answers.get(SbaSectionFinishedPage(Rentals)).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_structures_and_building_allowance_link"
    )

  private def propertyAllowancesItem(taxYear: Int, userAnswers: Option[UserAnswers]) =
    TaskListItem(
      "summary.allowances",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "allowances", Rentals, NormalMode) {
          controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear, Rentals)
        }(identity),
      userAnswers
        .flatMap { answers =>
          answers.get(AllowancesSectionFinishedPage).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_allowances_link"
    )

  private def propertyRentalsExpensesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.expenses",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "expenses", Rentals, NormalMode) {
          controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear, Rentals)
        }(identity),
      userAnswers
        .flatMap { answers =>
          answers.get(ExpensesSectionFinishedPage).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_expenses_link"
    )

  private def propertyRentalsIncomeItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.income",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "income", Rentals, NormalMode) {

          controllers.propertyrentals.income.routes.PropertyIncomeStartController.onPageLoad(taxYear)
        }(identity),
      userAnswers
        .flatMap { answers =>
          answers.get(IncomeSectionFinishedPage).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_income_link"
    )

  private def propertyRentalsAboutItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.about",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "about", Rentals, NormalMode) {
          controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(AboutPropertyRentalsSectionFinishedPage))

        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage(Rentals))).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rentals_about_link"
    )

  private def ukRentARoomAboutItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.about",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "about", RentARoom, NormalMode) {
          controllers.ukrentaroom.routes.RentARoomStartController.onPageLoad(taxYear)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(AboutSectionCompletePage))

        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(JointlyLetPage(RentARoom))).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rent_a_room_about_link"
    )

  private def rentalsAndRaRAboutItem(taxYear: Int, userAnswers: Option[UserAnswers]) =
    TaskListItem(
      "summary.about",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "about", RentalsRentARoom, NormalMode) {

          controllers.rentalsandrentaroom.routes.RentalsRentARoomStartController.onPageLoad(taxYear)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(RentalsRaRAboutCompletePage))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (
            userAnswers
              .flatMap(_.get(ConsolidatedExpensesRRPage))
              .isDefined || userAnswers.flatMap(_.get(RentsRatesAndInsuranceRRPage)).isDefined
          ) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rentals_and_rent_a_room_about_link"
    )

  private def rentalsAndRaRAdjustmentsItem(taxYear: Int, userAnswers: Option[UserAnswers]): TaskListItem =
    TaskListItem(
      "summary.adjustments",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "adjustments", RentalsRentARoom, NormalMode) {
          controllers.rentalsandrentaroom.adjustments.routes.RentalsAndRentARoomAdjustmentsStartController
            .onPageLoad(
              taxYear,
              userAnswers
                .flatMap(_.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom)))
                .getOrElse(false)
            )
        }(identity),
      getTaskListTagStatus(userAnswers, RentalsRentARoom),
      "rentals_and_rent_a_room_adjustments_link"
    )

  private def rentalsAndRaRIncomeItem(taxYear: Int, userAnswers: Option[UserAnswers]) =
    TaskListItem(
      "summary.income",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "income", RentalsRentARoom, NormalMode) {

          controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeStartController.onPageLoad(taxYear)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(RentalsRaRIncomeCompletePage))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(IncomeSectionFinishedPage)).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rentals_and_rent_a_room_income_link"
    )

  private def rentalsAndRaRExpensesItem(taxYear: Int, userAnswers: Option[UserAnswers]) =
    TaskListItem(
      "summary.expenses",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "expenses", RentalsRentARoom, NormalMode) {

          controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear, RentalsRentARoom)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(RentalsRaRExpensesCompletePage))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (
            userAnswers
              .flatMap(_.get(ConsolidatedExpensesPage(RentalsRentARoom)))
              .isDefined || userAnswers.flatMap(_.get(RentsRatesAndInsurancePage(RentalsRentARoom))).isDefined
          ) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rentals_and_rent_a_room_expenses_link"
    )

  private def rentalsAndRaRAllowancesItem(taxYear: Int, userAnswers: Option[UserAnswers]) =
    TaskListItem(
      "summary.allowances",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "allowances", RentalsRentARoom, NormalMode) {
          controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear, RentalsRentARoom)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(RentalsRaRAllowancesCompletePage))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (
            userAnswers
              .flatMap(_.get(AnnualInvestmentAllowancePage(RentalsRentARoom)))
              .isDefined || userAnswers.flatMap(_.get(CapitalAllowancesForACarPage(RentalsRentARoom))).isDefined
          ) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rentals_and_rent_a_room_allowances_link"
    )

  private def rentalsAndRaRSBAItem(taxYear: Int, userAnswers: Option[UserAnswers]) =
    TaskListItem(
      "summary.structuresAndBuildingAllowance",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "sba", RentalsRentARoom, NormalMode) {
          controllers.structuresbuildingallowance.routes.ClaimStructureBuildingAllowanceController
            .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(SbaSectionFinishedPage(RentalsRentARoom)))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(ClaimStructureBuildingAllowancePage(RentalsRentARoom))).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rentals_and_rent_a_room_structures_and_building_allowance_link"
    )

  private def rentalsAndRaRESBAItem(taxYear: Int, userAnswers: Option[UserAnswers]) =
    TaskListItem(
      "summary.enhancedStructuresAndBuildingAllowance",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "esba", RentalsRentARoom, NormalMode) {
          controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController
            .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(EsbaSectionFinishedPage(RentalsRentARoom)))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(ClaimEsbaPage(RentalsRentARoom))).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rentals_and_rent_a_room_enhanced_structures_and_building_allowance_link"
    )

  private def ukRentARoomExpensesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.expenses",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "expenses", RentARoom, NormalMode) {
          controllers.ukrentaroom.expenses.routes.UkRentARoomExpensesIntroController.onPageLoad(taxYear)
        }(identity), {

        val sectionFinished = userAnswers.flatMap(_.get(ExpensesRRSectionCompletePage))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (
            userAnswers
              .flatMap(_.get(ConsolidatedExpensesRRPage))
              .isDefined || userAnswers.flatMap(_.get(RentsRatesAndInsuranceRRPage)).isDefined
          ) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rent_a_room_expenses_link"
    )

  private def ukRentARoomAllowancesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.allowances",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "allowances", RentARoom, NormalMode) {

          controllers.ukrentaroom.allowances.routes.RRAllowancesStartController.onPageLoad(taxYear)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(RaRAllowancesCompletePage))
        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (
            userAnswers
              .flatMap(_.get(RaRElectricChargePointAllowanceForAnEVPage))
              .isDefined || userAnswers.flatMap(_.get(RaRCapitalAllowancesForACarPage)).isDefined
          ) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rent_a_room_allowances_link"
    )

  private def ukRentARoomAdjustmentsItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.adjustments",
      cyaDiversionService
        .redirectToCYAIfFinished[Call](taxYear, userAnswers, "adjustments", RentARoom, NormalMode) {
          controllers.ukrentaroom.adjustments.routes.RaRAdjustmentsIntroController.onPageLoad(taxYear)
        }(identity), {
        val sectionFinished = userAnswers.flatMap(_.get(RaRAdjustmentsCompletePage))

        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(RaRBalancingChargePage)).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rent_a_room_adjustments_link"
    )

  def getTaskListTagStatus(userAnswers: Option[UserAnswers], propertyType: PropertyType): TaskListTag.TaskListTag = {
    val claimPIA = userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage(propertyType)))
    val sectionFinished = propertyType match {
      case Rentals          => userAnswers.flatMap(_.get(RentalsAdjustmentsCompletePage))
      case RentalsRentARoom => userAnswers.flatMap(_.get(RentalsRaRAdjustmentsCompletePage))
    }
    val incomeSectionFinishedPage = propertyType match {
      case Rentals          => userAnswers.flatMap(_.get(IncomeSectionFinishedPage))
      case RentalsRentARoom => userAnswers.flatMap(_.get(RentalsRaRIncomeCompletePage))
    }
    (claimPIA, incomeSectionFinishedPage, sectionFinished) match {
      case (Some(true), None, None)              => TaskListTag.CanNotStart
      case (Some(true), Some(false), _)          => TaskListTag.CanNotStart
      case (Some(true), Some(true), None)        => TaskListTag.NotStarted
      case (Some(true), Some(true), Some(false)) => TaskListTag.InProgress
      case (Some(true), Some(true), Some(true))  => TaskListTag.Completed
      case (Some(false), _, None)                => TaskListTag.NotStarted
      case (Some(false), _, Some(true))          => TaskListTag.Completed
      case (Some(false), _, Some(false))         => TaskListTag.InProgress
      case (_, _, _)                             => TaskListTag.NotStarted
    }
  }

}
