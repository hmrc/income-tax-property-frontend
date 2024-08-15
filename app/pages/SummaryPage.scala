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

import models.{NormalMode, RentARoom, Rentals, RentalsRentARoom, UKPropertySelect, UserAnswers}
import pages.adjustments.{PrivateUseAdjustmentPage, RentalsAdjustmentsCompletePage}
import pages.allowances.AllowancesSectionFinishedPage
import pages.enhancedstructuresbuildingallowance.EsbaSectionFinishedPage
import pages.propertyrentals.expenses.{ConsolidatedExpensesPage, ExpensesSectionFinishedPage, RentsRatesAndInsurancePage}
import pages.propertyrentals.income.IncomeSectionFinishedPage
import pages.propertyrentals.{AboutPropertyRentalsSectionFinishedPage, ClaimPropertyIncomeAllowancePage}
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import pages.rentalsandrentaroom.expenses.RentalsRaRExpensesCompletePage
import pages.rentalsandrentaroom.income.RentalsRaRIncomeCompletePage
import pages.structurebuildingallowance.SbaSectionFinishedPage
import pages.ukrentaroom.adjustments.{RaRAdjustmentsCompletePage, RaRBalancingChargePage}
import pages.ukrentaroom.allowances.{RaRAllowancesCompletePage, RaRCapitalAllowancesForACarPage, RaRElectricChargePointAllowanceForAnEVPage}
import pages.ukrentaroom.expenses.{ConsolidatedExpensesRRPage, ExpensesRRSectionCompletePage, RentsRatesAndInsuranceRRPage}
import pages.ukrentaroom.{AboutSectionCompletePage, ClaimExpensesOrReliefPage, JointlyLetPage}
import viewmodels.summary.{TaskListItem, TaskListTag}

case object SummaryPage {
  def createUkPropertyRows(
    userAnswers: Option[UserAnswers],
    taxYear: Int,
    cashOrAccruals: Boolean
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
          case false if cashOrAccruals =>
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

  def createRentalsAndRentARoomRows(userAnswers: Option[UserAnswers], taxYear: Int): Seq[TaskListItem] = {
    val isRentARoomSelected = isSelected(userAnswers, UKPropertySelect.RentARoom)
    val isPropertyRentalsSelected = isSelected(userAnswers, UKPropertySelect.PropertyRentals)
    val aboutItem = rentalsAndRaRAboutItem(userAnswers, taxYear)
    val incomeItem = rentalsAndRaRIncomeItem(userAnswers, taxYear)

    if (isRentARoomSelected && isPropertyRentalsSelected) {
      userAnswers.flatMap(_.get(RentalsRaRAboutCompletePage)) match {
        case None => Seq(aboutItem)
        case Some(_) =>
          val baseItems = Seq(aboutItem, incomeItem)
          userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom))) match {
            case Some(false) =>
              baseItems concat Seq(rentalsAndRaRExpensesItem(taxYear, userAnswers), rentalsAndRaRAllowancesItem(taxYear))
            case _ => baseItems
          }
      }
    } else {
      Seq.empty[TaskListItem]
    }
  }

  def propertyAboutItems(userAnswers: Option[UserAnswers], taxYear: Int): Seq[TaskListItem] =
    Seq(
      TaskListItem(
        "summary.about",
        controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear), {
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
      controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController.onPageLoad(taxYear, NormalMode),
      userAnswers
        .flatMap { answers =>
          answers.get(EsbaSectionFinishedPage).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_enhanced_structures_and_building_allowance_link"
    )

  private def propertyRentalsAdjustmentsItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.adjustments",
      controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear), {
        val sectionFinished = userAnswers.flatMap(_.get(RentalsAdjustmentsCompletePage))

        sectionFinished.map(userChoice => if (userChoice) TaskListTag.Completed else TaskListTag.InProgress).getOrElse {
          if (userAnswers.flatMap(_.get(PrivateUseAdjustmentPage)).isDefined) {
            TaskListTag.InProgress
          } else {
            TaskListTag.NotStarted
          }
        }
      },
      "rentals_adjustments_link"
    )

  private def structuresAndBuildingAllowanceItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.structuresAndBuildingAllowance",
      controllers.structuresbuildingallowance.routes.ClaimStructureBuildingAllowanceController
        .onPageLoad(taxYear, NormalMode),
      userAnswers
        .flatMap { answers =>
          answers.get(SbaSectionFinishedPage).map { finishedYesOrNo =>
            if (finishedYesOrNo) TaskListTag.Completed else TaskListTag.InProgress
          }
        }
        .getOrElse(TaskListTag.NotStarted),
      "rentals_structures_and_building_allowance_link"
    )

  private def propertyAllowancesItem(taxYear: Int, userAnswers: Option[UserAnswers]) =
    TaskListItem(
      "summary.allowances",
      controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear, Rentals),
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
      controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear, Rentals),
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
      controllers.propertyrentals.income.routes.PropertyIncomeStartController.onPageLoad(taxYear),
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
      controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear), {
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
      controllers.ukrentaroom.routes.RentARoomStartController.onPageLoad(taxYear), {
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

  private def rentalsAndRaRAboutItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.about",
      controllers.rentalsandrentaroom.routes.RentalsRentARoomStartController.onPageLoad(taxYear), {

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

  private def rentalsAndRaRIncomeItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.income",
      controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeStartController.onPageLoad(taxYear), {
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
      controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear, RentalsRentARoom),
      {
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

  private def rentalsAndRaRAllowancesItem(taxYear: Int) =
    TaskListItem(
      "summary.allowances",
      controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear, RentalsRentARoom),
      TaskListTag.NotStarted,
      "rentals_and_rent_a_room_allowances_link"
    )

  private def ukRentARoomExpensesItem(userAnswers: Option[UserAnswers], taxYear: Int) =
    TaskListItem(
      "summary.expenses",
      controllers.ukrentaroom.expenses.routes.UkRentARoomExpensesIntroController.onPageLoad(taxYear), {

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
      controllers.ukrentaroom.allowances.routes.RRAllowancesStartController.onPageLoad(taxYear), {
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
      controllers.ukrentaroom.adjustments.routes.RaRAdjustmentsIntroController.onPageLoad(taxYear), {
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

  private def isSelected(userAnswers: Option[UserAnswers], select: UKPropertySelect): Boolean =
    userAnswers.exists(_.get(UKPropertyPage).exists(_.contains(select)))

}
