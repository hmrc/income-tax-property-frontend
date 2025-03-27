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

package pages.ukandforeignproperty

import controllers.foreign.adjustments.routes.ForeignAdjustmentsStartController
import controllers.foreign.allowances.routes.ForeignPropertyAllowancesStartController
import controllers.foreign.expenses.routes.ForeignPropertyExpensesStartController
import controllers.foreign.income.routes.ForeignPropertyIncomeStartController
import controllers.foreign.routes.ForeignIncomeTaxController
import controllers.foreign.structuresbuildingallowance.routes.ForeignClaimStructureBuildingAllowanceController
import controllers.ukandforeignproperty.routes.UkAndForeignPropertyCheckYourAnswersController
import models.{NormalMode, Rentals, RentalsRentARoom, UkAndForeignPropertyClaimExpensesOrRelief, UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses, UkAndForeignPropertyRentalTypeUk, UserAnswers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.UkAndForeignPropertyRentalTypeUkPage
import pages.foreign.Country
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import service.{BusinessService, CYADiversionService, ForeignCYADiversionService, UkAndForeignCYADiversionService}
import viewmodels.summary.TaskListItem
import viewmodels.summary.TaskListTag.{Completed, InProgress, NotStarted}

import java.time.LocalDate

class UkAndForeignPropertySummaryPageSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  val cyaDiversionService: CYADiversionService = new CYADiversionService()
  val foreignCYADiversionService: ForeignCYADiversionService = new ForeignCYADiversionService()
  val ukAndForeignCYADiversionService: UkAndForeignCYADiversionService = new UkAndForeignCYADiversionService()
  val mockBusinessService: BusinessService = mock[BusinessService]
  val countryCode: String = "AUS"

  def emptyUserAnswers: UserAnswers = UserAnswers("userAnswersId")

  private val taxYear = LocalDate.now.getYear

  ".UkAndForeignPropertySummaryPage 'About' section" should {

    "should show InProgress if the user selects false for Have you finished" in {
      val userAnswers: Option[UserAnswers] = emptyUserAnswers
        .set(
          SectionCompletePage,
          false
        )
        .toOption

      val taskList = UkAndForeignPropertySummaryPage.aboutItems(taxYear, userAnswers, ukAndForeignCYADiversionService)
      taskList.head.taskListTag shouldBe InProgress
    }

    "should show Complete if the user selects true for Have you finished" in {
      val userAnswers: Option[UserAnswers] = emptyUserAnswers
        .set(
          SectionCompletePage,
          true
        )
        .toOption

      val taskList = UkAndForeignPropertySummaryPage.aboutItems(taxYear, userAnswers, ukAndForeignCYADiversionService)
      taskList.head.taskListTag shouldBe Completed
    }

    "should show NotStarted if the user has not selected any value for Have you finished" in {
      val userAnswers: Option[UserAnswers] = Some(emptyUserAnswers)

      val taskList = UkAndForeignPropertySummaryPage.aboutItems(taxYear, userAnswers, ukAndForeignCYADiversionService)
      taskList.head.taskListTag shouldBe NotStarted
    }

    "should redirect to the CYA page when the task list tag is 'Completed'" in {
      val userAnswers: Option[UserAnswers] = emptyUserAnswers
        .set(
          SectionCompletePage,
          true
        )
        .toOption
      val taskList = UkAndForeignPropertySummaryPage.aboutItems(taxYear, userAnswers, ukAndForeignCYADiversionService)
      taskList.head.call shouldBe UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    }
  }

  "Uk Property section" should {

    "show the correct property rental items" when {
      import UkRentalItems._
      val rentalsUA = emptyUserAnswers
        .set(SectionCompletePage, true)
        .flatMap(
          _.set(
            UkAndForeignPropertyRentalTypeUkPage,
            Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.PropertyRentals)
          )
        )
        .get
      "The client uses accruals based accounting " +
        "'Property rentals' selected and claiming PIA" in {
        val isClaimingPIA = true
        val isClaimingRentARoomRelief = false
        val isAccruals = true
        val userAnswers = rentalsUA
          .set(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
          )
          .toOption

        val (rentalsTaskList, _, _) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
          taxYear,
          userAnswers,
          isClaimingPIA,
          isClaimingRentARoomRelief,
          isAccruals,
          cyaDiversionService
        )
        val res = Seq(
          adjustmentsItem(isClaimingPIA)
        )
        rentalsTaskList shouldBe res
      }

      "The client uses accruals based accounting " +
        "'Property rentals' selected and not claiming PIA" in {
        val isClaimingPIA = false
        val isClaimingRentARoomRelief = false
        val isAccruals = true
        val userAnswers = rentalsUA
          .set(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
          )
          .toOption

        val (rentalsTaskList, _, _) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
          taxYear,
          userAnswers,
          isClaimingPIA,
          isClaimingRentARoomRelief,
          isAccruals,
          cyaDiversionService
        )
        val res = Seq(
          incomeItem,
          expensesItem,
          allowancesItem,
          sbaItem,
          esbaItem,
          adjustmentsItem(isClaimingPIA)
        )
        rentalsTaskList shouldBe res
      }

      "The client uses cash based accounting " +
        "'Property rentals' selected and claiming PIA" in {
        val isClaimingPIA = true
        val isClaimingRentARoomRelief = false
        val isAccruals = false
        val userAnswers = rentalsUA
          .set(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
          )
          .toOption

        val (rentalsTaskList, _, _) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
          taxYear,
          userAnswers,
          isClaimingPIA,
          isClaimingRentARoomRelief,
          isAccruals,
          cyaDiversionService
        )
        val res = Seq(
          adjustmentsItem(isClaimingPIA)
        )
        rentalsTaskList shouldBe res
      }

      "The client uses cash based accounting " +
        "'Property rentals' selected and not claiming PIA" in {
        val isClaimingPIA = false
        val isClaimingRentARoomRelief = false
        val isAccruals = false
        val userAnswers = rentalsUA
          .set(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
          )
          .toOption

        val (rentalsTaskList, _, _) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
          taxYear,
          userAnswers,
          isClaimingPIA,
          isClaimingRentARoomRelief,
          isAccruals,
          cyaDiversionService
        )
        val res = Seq(
          incomeItem,
          expensesItem,
          allowancesItem,
          adjustmentsItem(isClaimingPIA)
        )
        rentalsTaskList shouldBe res
      }
    }

    "show the correct property rent a room items" when {
      import UkRentARoomItems._
      val rentARoomUA = emptyUserAnswers
        .set(SectionCompletePage, true)
        .flatMap(
          _.set(
            UkAndForeignPropertyRentalTypeUkPage,
            Set[UkAndForeignPropertyRentalTypeUk](
              UkAndForeignPropertyRentalTypeUk.RentARoom
            )
          )
        )
        .get

      "'Rent a room' selected and claiming:  Rent a room relief - PIA" in {
        val isClaimingRelief = true
        val isClaimingPIA = true
        val isAccruals = true

        val userAnswers = rentARoomUA
          .set(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
          )
          .flatMap(
            _.set(
              UkAndForeignPropertyClaimExpensesOrReliefPage,
              UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
            )
          )
          .toOption

        val (_, rentARoomTaskList, _) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
          taxYear,
          userAnswers,
          isClaimingPIA,
          isClaimingRelief,
          isAccruals,
          cyaDiversionService
        )
        val res = Seq(
          aboutItem
        )
        rentARoomTaskList shouldBe res
      }

      "'Rent a room' selected and claiming:  Rent a room relief - Expenses" in {
        val isClaimingRelief = true
        val isClaimingPIA = false
        val isAccruals = false
        val userAnswers = rentARoomUA
          .set(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
          )
          .flatMap(
            _.set(
              UkAndForeignPropertyClaimExpensesOrReliefPage,
              UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
            )
          )
          .toOption

        val (_, rentARoomTaskList, _) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
          taxYear,
          userAnswers,
          isClaimingPIA,
          isClaimingRelief,
          isAccruals,
          cyaDiversionService
        )
        val res = Seq(
          aboutItem
        )
        rentARoomTaskList shouldBe res
      }

      "'Rent a room' selected and claiming:  Expenses - PIA" in {
        val isClaimingRelief = false
        val isClaimingPIA = true
        val isAccruals = true

        val userAnswers = rentARoomUA
          .set(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
          )
          .flatMap(
            _.set(
              UkAndForeignPropertyClaimExpensesOrReliefPage,
              UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
            )
          )
          .toOption

        val (_, rentARoomTaskList, _) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
          taxYear,
          userAnswers,
          isClaimPIA = isClaimingPIA,
          isClaimRelief = isClaimingRelief,
          isAccruals,
          cyaDiversionService
        )
        val res = Seq(
          aboutItem,
          expensesItem,
          allowancesItem,
          adjustmentsItem
        )
        rentARoomTaskList shouldBe res
      }

      "'Rent a room' selected and claiming:  Expenses - Expenses" in {
        val isClaimingRelief = false
        val isClaimingPIA = false
        val isAccruals = false
        val userAnswers = rentARoomUA
          .set(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
          )
          .flatMap(
            _.set(
              UkAndForeignPropertyClaimExpensesOrReliefPage,
              UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
            )
          )
          .toOption

        val (_, rentARoomTaskList, _) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
          taxYear,
          userAnswers,
          isClaimingPIA,
          isClaimingRelief,
          isAccruals,
          cyaDiversionService
        )
        val res = Seq(
          aboutItem,
          expensesItem,
          allowancesItem,
          adjustmentsItem
        )
        rentARoomTaskList shouldBe res
      }
    }

    "show the correct property rentals and rent a room items" when {
      import UkRentalAndRentARoomItems._
      val rentalsRentARoomUA = emptyUserAnswers
        .set(SectionCompletePage, true)
        .flatMap(
          _.set(
            UkAndForeignPropertyRentalTypeUkPage,
            Set[UkAndForeignPropertyRentalTypeUk](
              UkAndForeignPropertyRentalTypeUk.PropertyRentals,
              UkAndForeignPropertyRentalTypeUk.RentARoom
            )
          )
        )
        .get

      "the property rentals and rent a room about section is incomplete" in {
        val isAboutComplete = false
        val isClaimingRelief = true
        val isClaimingPIA = false
        val isAccruals = false

        val userAnswers = rentalsRentARoomUA
          .set(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
          )
          .flatMap(
            _.set(
              UkAndForeignPropertyClaimExpensesOrReliefPage,
              UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
            )
          )
          .toOption

        val (_, _, rentalsAndRentARoomTaskList) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
          taxYear,
          userAnswers,
          isClaimingPIA,
          isClaimingRelief,
          isAccruals,
          cyaDiversionService
        )
        val res = Seq(
          aboutItem(isAboutComplete)
        )
        rentalsAndRentARoomTaskList shouldBe res
      }

      "the property rentals and rent a room about section has been completed" when {

        "both 'Rentals', 'Rent a Room' selected and claiming:  Rent a room relief - PIA" in {
          val isAboutComplete = true
          val isClaimingRelief = true
          val isClaimingPIA = true
          val isAccruals = false

          val userAnswers = rentalsRentARoomUA
            .set(
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
            )
            .flatMap(
              _.set(
                UkAndForeignPropertyClaimExpensesOrReliefPage,
                UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
              )
            )
            .flatMap(_.set(RentalsRaRAboutCompletePage, isAboutComplete))
            .toOption

          val (_, _, rentalsAndRentARoomTaskList) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
            taxYear,
            userAnswers,
            isClaimingPIA,
            isClaimingRelief,
            isAccruals,
            cyaDiversionService
          )
          val res = Seq(
            aboutItem(isAboutComplete),
            adjustmentsItem(isClaimingPIA)
          )
          rentalsAndRentARoomTaskList shouldBe res
        }

        "both 'Rentals', 'Rent a Room' selected and claiming:  Rent a room relief - Expenses " +
          "and the client uses accruals based accounting" in {
          val isAboutComplete = true
          val isClaimingRelief = true
          val isClaimingPIA = false
          val isAccruals = true

          val userAnswers = rentalsRentARoomUA
            .set(
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
            )
            .flatMap(
              _.set(
                UkAndForeignPropertyClaimExpensesOrReliefPage,
                UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
              )
            )
            .flatMap(_.set(RentalsRaRAboutCompletePage, isAboutComplete))
            .toOption

          val (_, _, rentalsAndRentARoomTaskList) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
            taxYear,
            userAnswers,
            isClaimingPIA,
            isClaimingRelief,
            isAccruals,
            cyaDiversionService
          )
          val res = Seq(
            aboutItem(isAboutComplete),
            incomeItem,
            expensesItem,
            allowancesItem,
            sbaItem,
            esbaItem,
            adjustmentsItem(isClaimingPIA)
          )
          rentalsAndRentARoomTaskList shouldBe res
        }

        "both 'Rentals', 'Rent a Room' selected and claiming:  Rent a room relief - Expenses " +
          "and the client uses cash based accounting" in {
          val isAboutComplete = true
          val isClaimingRelief = true
          val isClaimingPIA = false
          val isAccruals = false

          val userAnswers = rentalsRentARoomUA
            .set(
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
            )
            .flatMap(
              _.set(
                UkAndForeignPropertyClaimExpensesOrReliefPage,
                UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
              )
            )
            .flatMap(_.set(RentalsRaRAboutCompletePage, isAboutComplete))
            .toOption

          val (_, _, rentalsAndRentARoomTaskList) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
            taxYear,
            userAnswers,
            isClaimingPIA,
            isClaimingRelief,
            isAccruals,
            cyaDiversionService
          )
          val res = Seq(
            aboutItem(isAboutComplete),
            incomeItem,
            expensesItem,
            allowancesItem,
            adjustmentsItem(isClaimingPIA)
          )
          rentalsAndRentARoomTaskList shouldBe res
        }

        "both 'Rentals', 'Rent a Room' selected and claiming:  Expenses - PIA" in {
          val isAboutComplete = true
          val isClaimingRelief = false
          val isClaimingPIA = true
          val isAccruals = true
          val userAnswers =rentalsRentARoomUA
            .set(
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
            )
            .flatMap(
              _.set(
                UkAndForeignPropertyClaimExpensesOrReliefPage,
                UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
              )
            )
            .flatMap(_.set(RentalsRaRAboutCompletePage, isAboutComplete))
            .toOption

          val (_, _, rentalsAndRentARoomTaskList) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
            taxYear,
            userAnswers,
            isClaimingPIA,
            isClaimingRelief,
            isAccruals,
            cyaDiversionService
          )
          val res = Seq(
            aboutItem(isAboutComplete),
            expensesItem,
            allowancesItem,
            adjustmentsItem(isClaimingPIA)
          )
          rentalsAndRentARoomTaskList shouldBe res
        }

        "both 'Rentals', 'Rent a Room' selected and claiming:  Expenses - Expenses " +
          "and the client uses accruals based accounting" in {
          val isClaimingRelief = false
          val isClaimingPIA = false
          val isAboutComplete = true
          val isAccruals = true

          val userAnswers = rentalsRentARoomUA
            .set(
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
            )
            .flatMap(
              _.set(
                UkAndForeignPropertyClaimExpensesOrReliefPage,
                UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
              )
            )
            .flatMap(_.set(RentalsRaRAboutCompletePage, isAboutComplete))
            .toOption

          val (_, _, rentalsAndRentARoomTaskList) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
            taxYear,
            userAnswers,
            isClaimingPIA,
            isClaimingRelief,
            isAccruals,
            cyaDiversionService
          )
          val res = Seq(
            aboutItem(isAboutComplete),
            incomeItem,
            expensesItem,
            allowancesItem,
            sbaItem,
            esbaItem,
            adjustmentsItem(isClaimingPIA)
          )
          rentalsAndRentARoomTaskList shouldBe res
        }

        "both 'Rentals', 'Rent a Room' selected and claiming:  Expenses - Expenses " +
          "and the client uses cash based accounting" in {
          val isClaimingRelief = false
          val isClaimingPIA = false
          val isAboutComplete = true
          val isAccruals = false

          val userAnswers = rentalsRentARoomUA
            .set(
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
            )
            .flatMap(
              _.set(
                UkAndForeignPropertyClaimExpensesOrReliefPage,
                UkAndForeignPropertyClaimExpensesOrRelief(isClaimingRelief)
              )
            )
            .flatMap(_.set(RentalsRaRAboutCompletePage, isAboutComplete))
            .toOption

          val (_, _, rentalsAndRentARoomTaskList) = UkAndForeignPropertySummaryPage.getUkPropertyTaskListItems(
            taxYear,
            userAnswers,
            isClaimingPIA,
            isClaimingRelief,
            isAccruals,
            cyaDiversionService
          )
          val res = Seq(
            aboutItem(isAboutComplete),
            incomeItem,
            expensesItem,
            allowancesItem,
            adjustmentsItem(isClaimingPIA)
          )
          rentalsAndRentARoomTaskList shouldBe res
        }
      }

    }
  }

  "Foreign Property section" should {
    import ForeignItems._

    "show the correct items when claiming PIA" in {
      val isClaimingPIA = false
      val isAccruals = true
      val userAnswers = UserAnswers("foreign-property-items")
        .set(
          UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
          UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
        )
        .flatMap(_.set(SelectCountryPage, List(Country("Spain", "ESP"))))
        .toOption

      val taskList: Seq[TaskListItem] = UkAndForeignPropertySummaryPage.getTaskListForForeignCountry(
        taxYear,
        isClaimingPIA,
        isAccruals,
        countryCode,
        userAnswers,
        foreignCYADiversionService
      )

      val res = Seq(
        foreignTaxItem,
        foreignIncomeItem,
        foreignExpensesItem,
        foreignAllowancesItem,
        foreignSbaItem,
        foreignAdjustmentsItem(isClaimingPIA)
      )
      taskList shouldBe res
    }

    "show the correct items when not claiming PIA" +
      " and the client uses accruals based accounting" in {
      val isClaimingPIA = false
      val isAccruals = true
      val userAnswers = UserAnswers("foreign-property-items")
        .set(
          UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
          UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
        )
        .flatMap(_.set(SelectCountryPage, List(Country("Spain", "ESP"))))
        .toOption

      val taskList: Seq[TaskListItem] = UkAndForeignPropertySummaryPage.getTaskListForForeignCountry(
        taxYear,
        isClaimingPIA,
        isAccruals,
        countryCode,
        userAnswers,
        foreignCYADiversionService
      )
      val res = Seq(
        foreignTaxItem,
        foreignIncomeItem,
        foreignExpensesItem,
        foreignAllowancesItem,
        foreignSbaItem,
        foreignAdjustmentsItem(isClaimingPIA)
      )
      taskList shouldBe res
    }

    "show the correct items when not claiming PIA" +
      " and the client uses cash based accounting" in {
      val isClaimingPIA = false
      val isAccruals = false
      val userAnswers = UserAnswers("foreign-property-items")
        .set(
          UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
          UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(isClaimingPIA)
        )
        .flatMap(
          _.set(
            UkAndForeignPropertyRentalTypeUkPage,
            Set[UkAndForeignPropertyRentalTypeUk](
              UkAndForeignPropertyRentalTypeUk.PropertyRentals,
              UkAndForeignPropertyRentalTypeUk.RentARoom
            )
          )
        )
        .flatMap(_.set(SelectCountryPage, List(Country("Spain", "ESP"))))
        .toOption

      val taskList: Seq[TaskListItem] = UkAndForeignPropertySummaryPage.getTaskListForForeignCountry(
        taxYear,
        isClaimingPIA,
        isAccruals,
        countryCode,
        userAnswers,
        foreignCYADiversionService
      )
      val res = Seq(
        foreignTaxItem,
        foreignIncomeItem,
        foreignExpensesItem,
        foreignAllowancesItem,
        foreignAdjustmentsItem(isClaimingPIA)
      )
      taskList shouldBe res
    }
  }

  object UkRentalItems {
    val incomeItem: TaskListItem =
      TaskListItem(
        "summary.income",
        call = controllers.propertyrentals.income.routes.PropertyIncomeStartController.onPageLoad(taxYear),
        NotStarted,
        "rentals_income_link"
      )
    val expensesItem: TaskListItem =
      TaskListItem(
        "summary.expenses",
        controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear, Rentals),
        NotStarted,
        "rentals_expenses_link"
      )
    val allowancesItem: TaskListItem =
      TaskListItem(
        "summary.allowances",
        controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear, Rentals),
        NotStarted,
        "rentals_allowances_link"
      )
    val sbaItem: TaskListItem =
      TaskListItem(
        "summary.structuresAndBuildingAllowance",
        controllers.structuresbuildingallowance.routes.ClaimStructureBuildingAllowanceController
          .onPageLoad(taxYear, NormalMode, Rentals),
        NotStarted,
        "rentals_structures_and_building_allowance_link"
      )
    val esbaItem: TaskListItem =
      TaskListItem(
        "summary.enhancedStructuresAndBuildingAllowance",
        controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController
          .onPageLoad(taxYear, NormalMode, Rentals),
        NotStarted,
        "rentals_enhanced_structures_and_building_allowance_link"
      )
    def adjustmentsItem(claimPIA: Boolean): TaskListItem = TaskListItem(
      content = "summary.adjustments",
      call = controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, claimPIA),
      taskListTag = NotStarted,
      id = "rentals_adjustments_link"
    )
  }

  object UkRentARoomItems {
    val aboutItem: TaskListItem =
      TaskListItem(
        "summary.about",
        controllers.ukrentaroom.routes.RentARoomStartController.onPageLoad(taxYear),
        NotStarted,
        "rent_a_room_about_link"
      )
    val incomeItem: TaskListItem =
      TaskListItem(
        "summary.income",
        call = controllers.propertyrentals.income.routes.PropertyIncomeStartController.onPageLoad(taxYear),
        NotStarted,
        "rentals_income_link"
      )
    val expensesItem: TaskListItem =
      TaskListItem(
        "summary.expenses",
        controllers.ukrentaroom.expenses.routes.UkRentARoomExpensesIntroController.onPageLoad(taxYear),
        NotStarted,
        "rent_a_room_expenses_link"
      )
    val allowancesItem: TaskListItem =
      TaskListItem(
        "summary.allowances",
        controllers.ukrentaroom.allowances.routes.RRAllowancesStartController.onPageLoad(taxYear),
        NotStarted,
        "rent_a_room_allowances_link"
      )
    val adjustmentsItem: TaskListItem = TaskListItem(
      "summary.adjustments",
      controllers.ukrentaroom.adjustments.routes.RaRAdjustmentsIntroController.onPageLoad(taxYear),
      NotStarted,
      "rent_a_room_adjustments_link"
    )
  }

  object UkRentalAndRentARoomItems {
    def aboutItem(isComplete: Boolean): TaskListItem =
      TaskListItem(
        "summary.about",
        if(isComplete){
          controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
        } else {
          controllers.rentalsandrentaroom.routes.RentalsRentARoomStartController.onPageLoad(taxYear)
        },
        if(isComplete) Completed else NotStarted,
        "rentals_and_rent_a_room_about_link"
      )
    val incomeItem: TaskListItem =
      TaskListItem(
        "summary.income",
        controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeStartController.onPageLoad(taxYear),
        NotStarted,
        "rentals_and_rent_a_room_income_link"
      )
    val expensesItem: TaskListItem =
      TaskListItem(
        "summary.expenses",
        controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear, RentalsRentARoom),
        NotStarted,
        "rentals_and_rent_a_room_expenses_link"
      )
    val allowancesItem: TaskListItem =
      TaskListItem(
        "summary.allowances",
        controllers.allowances.routes.AllowancesStartController.onPageLoad(taxYear, RentalsRentARoom),
        NotStarted,
        "rentals_and_rent_a_room_allowances_link"
      )
    val sbaItem: TaskListItem =
      TaskListItem(
        "summary.structuresAndBuildingAllowance",
        controllers.structuresbuildingallowance.routes.ClaimStructureBuildingAllowanceController
          .onPageLoad(taxYear, NormalMode, RentalsRentARoom),
        NotStarted,
        "rentals_and_rent_a_room_structures_and_building_allowance_link"
      )
    val esbaItem: TaskListItem =
      TaskListItem(
        "summary.enhancedStructuresAndBuildingAllowance",
        controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaController
          .onPageLoad(taxYear, NormalMode, RentalsRentARoom),
        NotStarted,
        "rentals_and_rent_a_room_enhanced_structures_and_building_allowance_link"
      )
    def adjustmentsItem(claimPIA: Boolean): TaskListItem = TaskListItem(
      content = "summary.adjustments",
      controllers.rentalsandrentaroom.adjustments.routes.RentalsAndRentARoomAdjustmentsStartController
        .onPageLoad(taxYear, claimPIA),
      NotStarted,
      "rentals_and_rent_a_room_adjustments_link"
    )
  }

  object ForeignItems {
    val foreignTaxItem: TaskListItem = TaskListItem(
      content = "foreign.tax",
      call = ForeignIncomeTaxController.onPageLoad(taxYear, countryCode, NormalMode),
      taskListTag = NotStarted,
      id = s"foreign_property_income_tax_$countryCode"
    )
    val foreignIncomeItem: TaskListItem = TaskListItem(
      "foreign.income",
      ForeignPropertyIncomeStartController.onPageLoad(taxYear, countryCode),
      NotStarted,
      s"foreign_property_income_$countryCode"
    )
    val foreignAllowancesItem: TaskListItem = TaskListItem(
      content = "summary.allowances",
      call = ForeignPropertyAllowancesStartController.onPageLoad(taxYear, countryCode),
      taskListTag = NotStarted,
      id = s"foreign_property_allowances_$countryCode"
    )
    val foreignExpensesItem: TaskListItem = TaskListItem(
      content = "summary.expenses",
      call = ForeignPropertyExpensesStartController.onPageLoad(taxYear, countryCode),
      taskListTag = NotStarted,
      id = s"foreign_property_expenses_$countryCode"
    )
    val foreignSbaItem: TaskListItem = TaskListItem(
      content = "summary.structuresAndBuildingAllowance",
      call = ForeignClaimStructureBuildingAllowanceController.onPageLoad(taxYear, countryCode, NormalMode),
      taskListTag = NotStarted,
      id = s"foreign_structure_and_building_allowance_$countryCode"
    )
    def foreignAdjustmentsItem(isPIA: Boolean): TaskListItem = TaskListItem(
      content = "summary.adjustments",
      call = ForeignAdjustmentsStartController.onPageLoad(taxYear, countryCode, isPIA),
      taskListTag = NotStarted,
      id = s"foreign_property_adjustments_$countryCode"
    )
  }

}
