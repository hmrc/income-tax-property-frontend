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

import controllers.foreign.adjustments.routes._
import controllers.foreign.allowances.routes._
import controllers.foreign.expenses.routes._
import controllers.foreign.income.routes._
import controllers.foreign.routes._
import controllers.foreign.structuresbuildingallowance.routes._
import models.{ForeignStructuresBuildingAllowanceAddress, NormalMode, UserAnswers}
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.allowances.ForeignAllowancesCompletePage
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import pages.foreign.income.ForeignIncomeSectionCompletePage
import pages.foreign.structurebuildingallowance._
import play.api.libs.json.Json
import service.ForeignCYADiversionService
import viewmodels.summary.TaskListItem
import viewmodels.summary.TaskListTag.{CanNotStart, Completed, InProgress, NotStarted}

import java.time.{Instant, LocalDate}

class ForeignPropertySummaryPageSpec
    extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with OptionValues with MockitoSugar {

  private val taxYear = 2024
  private val foreignCYADiversionService: ForeignCYADiversionService = new ForeignCYADiversionService()
  private val foreignSummaryPage: ForeignSummaryPage = ForeignSummaryPage(foreignCYADiversionService)
  val countryCode = "ESP"
  val foreignTaxItem: TaskListItem = TaskListItem(
    content = "foreign.tax",
    call = ForeignIncomeTaxController.onPageLoad(taxYear, countryCode, NormalMode),
    taskListTag = NotStarted,
    id = s"foreign_property_income_tax_$countryCode"
  )
  def incomeItem(isComplete: Boolean = false): TaskListItem = TaskListItem(
    content = "foreign.income",
    call = if(isComplete) ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode) else ForeignPropertyIncomeStartController.onPageLoad(taxYear, countryCode),
    taskListTag = if(isComplete) Completed else NotStarted,
    id =  s"foreign_property_income_$countryCode"
  )
  def adjustmentsItem(isPIA: Boolean, isIncomeSectionComplete: Boolean = false): TaskListItem = TaskListItem(
    content = "summary.adjustments",
    call = ForeignAdjustmentsStartController.onPageLoad(taxYear, countryCode, isPIA),
    taskListTag = (isPIA, isIncomeSectionComplete) match {
      case (true, false) => CanNotStart
      case (_, _) => NotStarted
    },
    id =  s"foreign_property_adjustments_$countryCode"
  )
  val allowancesItem: TaskListItem = TaskListItem(
    content = "summary.allowances",
    call = ForeignPropertyAllowancesStartController.onPageLoad(taxYear, countryCode),
    taskListTag = NotStarted,
    id =  s"foreign_property_allowances_$countryCode"
  )
  val expensesItem: TaskListItem = TaskListItem(
    content = "summary.expenses",
    call = ForeignPropertyExpensesStartController.onPageLoad(taxYear, countryCode),
    taskListTag = NotStarted,
    id =  s"foreign_property_expenses_$countryCode"
  )
  val sbaItem :TaskListItem = TaskListItem(
    content = "summary.structuresAndBuildingAllowance",
    call = ForeignClaimStructureBuildingAllowanceController.onPageLoad(taxYear, countryCode, NormalMode),
    taskListTag = NotStarted,
    id =  s"foreign_structure_and_building_allowance_$countryCode"
  )

  ".ForeignPropertySummaryPage for the Foreign property Select Country" - {
    "should show InProgress if the user selects false for Have you finished" in {
      val userAnswers = UserAnswers(
        id = "foreign-property-select",
        data = Json.obj(
          "selectCountrySectionComplete" -> false
        ),
        Instant.ofEpochSecond(1)
      )
      val taskList = foreignSummaryPage.foreignPropertyAboutItems(taxYear, Some(userAnswers))
      taskList.head.taskListTag shouldBe InProgress
    }

    "should show Complete if the user selects true for Have you finished" in {
      val userAnswers = UserAnswers(
        id = "foreign-property-select",
        data = Json.obj(
          "selectCountrySectionComplete" -> true
        ),
        Instant.ofEpochSecond(1)
      )
      val taskList = foreignSummaryPage.foreignPropertyAboutItems(taxYear, Some(userAnswers))
      taskList.head.taskListTag shouldBe Completed
    }

    "should show NotStarted if the user has not selected any value for Have you finished" in {
      val userAnswers = UserAnswers(
        id = "foreign-property-select",
        data = Json.obj(
          "selectCountrySectionComplete" -> ""
        ),
        Instant.ofEpochSecond(1)
      )
      val taskList = foreignSummaryPage.foreignPropertyAboutItems(taxYear, Some(userAnswers))
      taskList.head.taskListTag shouldBe NotStarted
    }

    "should redirect to the CYA page when the task list tag is 'Completed'" in {
      val userAnswers = UserAnswers(
        id = "foreign-property-select",
        data = Json.obj(
          "selectCountrySectionComplete" -> true
        ),
        Instant.ofEpochSecond(1)
      )
      val taskList = foreignSummaryPage.foreignPropertyAboutItems(taxYear, Some(userAnswers))
      taskList.head.call shouldBe ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    }
  }

  "foreignPropertyItems" - {
    "should show the correct items when claiming PIA and Income section is completed" in {
      val isClaimingPIA = true
      val accrualsOrCash = true
      val userAnswers = UserAnswers(id  = "foreign-property-items")
        .set(ClaimPropertyIncomeAllowanceOrExpensesPage, isClaimingPIA)
        .flatMap(_.set(ForeignIncomeSectionCompletePage(countryCode), true))
        .toOption

      val taskList: Seq[TaskListItem] = foreignSummaryPage.foreignPropertyItems(taxYear, accrualsOrCash, countryCode, userAnswers)
      val res = Seq(
        foreignTaxItem,
        incomeItem(isComplete = true),
        adjustmentsItem(isClaimingPIA, isIncomeSectionComplete = true)
      )
      taskList shouldBe res
    }

    "should show the correct items when claiming PIA and Income section is incomplete" in {
      val isClaimingPIA = true
      val accrualsOrCash = true
      val userAnswers = UserAnswers(id  = "foreign-property-items")
        .set(ClaimPropertyIncomeAllowanceOrExpensesPage, isClaimingPIA)
        .toOption

      val taskList: Seq[TaskListItem] = foreignSummaryPage.foreignPropertyItems(taxYear, accrualsOrCash, countryCode, userAnswers)
      val res = Seq(
        foreignTaxItem,
        incomeItem(),
        adjustmentsItem(isClaimingPIA)
      )
      taskList shouldBe res
    }

    "should show the correct items when not claiming PIA with accruals" in {
      val isClaimingPIA = false
      val accrualsOrCash = true
      val userAnswers = UserAnswers(id  = "foreign-property-items").set(ClaimPropertyIncomeAllowanceOrExpensesPage, isClaimingPIA).toOption
      val taskList: Seq[TaskListItem] = foreignSummaryPage.foreignPropertyItems(taxYear, accrualsOrCash, countryCode, userAnswers)
      val res = Seq(
        foreignTaxItem,
        incomeItem(),
        expensesItem,
        allowancesItem,
        sbaItem,
        adjustmentsItem(isClaimingPIA)
      )
      taskList shouldBe res
    }

    "should show the correct items when not claiming PIA with Cash" in {
      val isClaimingPIA = false
      val accrualsOrCash = false
      val userAnswers = UserAnswers(id  = "foreign-property-items").set(ClaimPropertyIncomeAllowanceOrExpensesPage, isClaimingPIA).toOption
      val taskList: Seq[TaskListItem] = foreignSummaryPage.foreignPropertyItems(taxYear, accrualsOrCash, countryCode, userAnswers)
      val res = Seq(
        foreignTaxItem,
        incomeItem(),
        expensesItem,
        allowancesItem,
        adjustmentsItem(isClaimingPIA)
      )
      taskList shouldBe res
    }

    "should not show adjustments when PIA has not been specified" in {
      val accrualsOrCash = true
      val userAnswers = UserAnswers(id  = "foreign-property-items")
      val taskList: Seq[TaskListItem] = foreignSummaryPage.foreignPropertyItems(taxYear, accrualsOrCash, countryCode, Some(userAnswers))
      val res = Seq(
        foreignTaxItem,
        incomeItem()
      )
      taskList shouldBe res
    }

    "should redirect to the CYA page when the task list tag is 'Completed' and SBA is not being claimed" in {
      val isClaimingPIA = false
      val accrualsOrCash = true
      val userAnswers = (for {
        ua <- UserAnswers(id = "foreign-property-items").set(ClaimPropertyIncomeAllowanceOrExpensesPage, isClaimingPIA)
        ua1 <- ua.set(ForeignTaxSectionCompletePage(countryCode), true)
        ua2 <- ua1.set(ForeignIncomeSectionCompletePage(countryCode), true)
        ua3 <- ua2.set(ForeignAllowancesCompletePage(countryCode), true)
        ua4 <- ua3.set(ForeignExpensesSectionCompletePage(countryCode), true)
        ua5 <- ua4.set(ForeignSbaCompletePage(countryCode), true)
        ua6 <- ua5.set(ForeignClaimStructureBuildingAllowancePage(countryCode), false)
      } yield ua6).toOption

      val taskList: Seq[TaskListItem] = foreignSummaryPage.foreignPropertyItems(taxYear, accrualsOrCash, countryCode, userAnswers)
      val res = Seq(
        foreignTaxItem.copy(taskListTag = Completed, call = ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        incomeItem().copy(taskListTag = Completed, call = ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        expensesItem.copy(taskListTag = Completed, call = ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        allowancesItem.copy(taskListTag = Completed, call = ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        sbaItem.copy(taskListTag = Completed, call = ForeignClaimSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        adjustmentsItem(isClaimingPIA)
      )
      taskList shouldBe res
    }

    "should redirect to the CYA page when the task list tag is 'Completed' and SBA is being claimed" in {
      val isClaimingPIA = false
      val accrualsOrCash = true
      val sbaAddress: ForeignStructureBuildingAllowance = ForeignStructureBuildingAllowance(
        foreignStructureBuildingAllowanceClaim = 1,
        foreignStructureBuildingQualifyingDate = LocalDate.now(),
        foreignStructureBuildingQualifyingAmount = 1,
        foreignStructureBuildingAddress = ForeignStructuresBuildingAllowanceAddress(
          name = "building-name",
          number = "1",
          postCode = "FF4 4FF"
        )
      )
      val userAnswers = (for {
        ua <- UserAnswers(id = "foreign-property-items").set(ClaimPropertyIncomeAllowanceOrExpensesPage, isClaimingPIA)
        ua1 <- ua.set(ForeignTaxSectionCompletePage(countryCode), true)
        ua2 <- ua1.set(ForeignIncomeSectionCompletePage(countryCode), true)
        ua3 <- ua2.set(ForeignAllowancesCompletePage(countryCode), true)
        ua4 <- ua3.set(ForeignExpensesSectionCompletePage(countryCode), true)
        ua5 <- ua4.set(ForeignSbaCompletePage(countryCode), true)
        ua6 <- ua5.set(ForeignClaimStructureBuildingAllowancePage(countryCode), true)
        ua7 <- ua6.set(ForeignStructureBuildingAllowanceGroup(countryCode), Array(sbaAddress))
      } yield ua7).toOption

      val taskList: Seq[TaskListItem] = foreignSummaryPage.foreignPropertyItems(taxYear, accrualsOrCash, countryCode, userAnswers)
      val res = Seq(
        foreignTaxItem.copy(taskListTag = Completed, call = ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        incomeItem().copy(taskListTag = Completed, call = ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        expensesItem.copy(taskListTag = Completed, call = ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        allowancesItem.copy(taskListTag = Completed, call = ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        sbaItem.copy(taskListTag = Completed, call = ForeignStructureBuildingAllowanceClaimsController.onPageLoad(taxYear, countryCode)),
        adjustmentsItem(isClaimingPIA)
      )
      taskList shouldBe res
    }

    "should redirect to the CYA page when the task list tag is 'Completed' and with no sba for Cash basis" in {
      val isClaimingPIA = false
      val accrualsOrCash = false
      val userAnswers = (for {
        ua <- UserAnswers(id = "foreign-property-items").set(ClaimPropertyIncomeAllowanceOrExpensesPage, isClaimingPIA)
        ua1 <- ua.set(ForeignTaxSectionCompletePage(countryCode), true)
        ua2 <- ua1.set(ForeignIncomeSectionCompletePage(countryCode), true)
        ua3 <- ua2.set(ForeignAllowancesCompletePage(countryCode), true)
        ua4 <- ua3.set(ForeignExpensesSectionCompletePage(countryCode), true)
        ua5 <- ua4.set(ForeignClaimStructureBuildingAllowancePage(countryCode), false)
      } yield ua5).toOption

      val taskList: Seq[TaskListItem] = foreignSummaryPage.foreignPropertyItems(taxYear, accrualsOrCash, countryCode, userAnswers)
      val res = Seq(
        foreignTaxItem.copy(taskListTag = Completed, call = ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        incomeItem().copy(taskListTag = Completed, call = ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        expensesItem.copy(taskListTag = Completed, call = ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        allowancesItem.copy(taskListTag = Completed, call = ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)),
        adjustmentsItem(isClaimingPIA)
      )
      taskList shouldBe res
    }
  }

}
