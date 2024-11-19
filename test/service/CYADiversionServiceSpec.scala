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

package service

import base.SpecBase
import models.{PropertyType, RentARoom, Rentals, RentalsRentARoom, UserAnswers}
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.adjustments.RentalsAdjustmentsCompletePage
import pages.allowances.AllowancesSectionFinishedPage
import pages.enhancedstructuresbuildingallowance.EsbaSectionFinishedPage
import pages.propertyrentals.AboutPropertyRentalsSectionFinishedPage
import pages.propertyrentals.expenses.ExpensesSectionFinishedPage
import pages.propertyrentals.income.IncomeSectionFinishedPage
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import pages.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompletePage
import pages.rentalsandrentaroom.allowances.RentalsRaRAllowancesCompletePage
import pages.rentalsandrentaroom.expenses.RentalsRaRExpensesCompletePage
import pages.rentalsandrentaroom.income.RentalsRaRIncomeCompletePage
import pages.structurebuildingallowance.SbaSectionFinishedPage
import pages.ukrentaroom.AboutSectionCompletePage
import pages.ukrentaroom.adjustments.RaRAdjustmentsCompletePage
import pages.ukrentaroom.allowances.RaRAllowancesCompletePage
import pages.ukrentaroom.expenses.ExpensesRRSectionCompletePage
import play.api.mvc.Call
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

class CYADiversionServiceSpec extends SpecBase with FutureAwaits with DefaultAwaitTimeout with Matchers {
  val cyaDiversionService = new CYADiversionService
  val taxYear = 2024
  val userAnswers = emptyUserAnswers
  private val rentalsAboutCYA =
    controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
  private val rentARoomAboutCYA = controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)
  private val rentalsAndRentARoomAboutCYA =
    controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)

  private val rentalsIncomeCYA =
    controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
  private val rentalsAndRentARoomIncomeCYA =
    controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
      .onPageLoad(taxYear)

  private val rentalsExpensesCYA =
    controllers.propertyrentals.expenses.routes.ExpensesCheckYourAnswersController.onPageLoad(taxYear)
  private val rentARoomExpensesCYA =
    controllers.ukrentaroom.expenses.routes.ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
  private val rentalsRentARoomExpensesCYA =
    controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController.onPageLoad(taxYear)

  private val rentalsAllowancesCYA =
    controllers.allowances.routes.AllowancesCheckYourAnswersController.onPageLoad(taxYear)
  private val rentARoomAllowancesCYA =
    controllers.ukrentaroom.allowances.routes.RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
  private val rentalsAndRentARoomAllowancesCYA =
    controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
      .onPageLoad(taxYear)

  private val rentalsAdjustmentsCYA =
    controllers.adjustments.routes.AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
  private val rentARoomAdjustmentsCYA =
    controllers.ukrentaroom.adjustments.routes.RaRAdjustmentsCYAController.onPageLoad(taxYear)
  private val rentalsAndRentARoomAdjustmentsCYA =
    controllers.rentalsandrentaroom.adjustments.routes.RentalsAndRentARoomAdjustmentsCheckYourAnswersController
      .onPageLoad(taxYear)
  private def esbasCYA(propertyType: PropertyType) =
    controllers.enhancedstructuresbuildingallowance.routes.ClaimEsbaCheckYourAnswersController
      .onPageLoad(taxYear, propertyType)

  private def sbasCYA(propertyType: PropertyType) =
    controllers.structuresbuildingallowance.routes.ClaimSbaCheckYourAnswersController.onPageLoad(taxYear, propertyType)

  val dummyCall = Call(
    "POST",
    "some url"
  )

  val scenarios = Table[UserAnswers, Call, PropertyType, String, String => String](
    ("userAnswers", "expected call", "property type", "journey name", "description"),
    (
      emptyUserAnswers.set(RentalsRaRAboutCompletePage, true).success.value,
      rentalsAndRentARoomAboutCYA,
      RentalsRentARoom,
      "about",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers,
      dummyCall,
      RentalsRentARoom,
      "about",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(AboutPropertyRentalsSectionFinishedPage, true).success.value,
      rentalsAboutCYA,
      Rentals,
      "about",
      description => s"$description for completed"
    ),
    (emptyUserAnswers, dummyCall, Rentals, "about", description => s"$description for NOT completed"),
    (
      emptyUserAnswers.set(AboutSectionCompletePage, true).success.value,
      rentARoomAboutCYA,
      RentARoom,
      "about",
      description => s"$description for completed"
    ),
    (emptyUserAnswers, dummyCall, RentARoom, "about", description => s"$description for NOT completed"),
    (
      emptyUserAnswers.set(IncomeSectionFinishedPage, true).success.value,
      rentalsIncomeCYA,
      Rentals,
      "income",
      description => s"$description for completed"
    ),
    (emptyUserAnswers, dummyCall, Rentals, "income", description => s"$description for NOT completed"),
    (
      emptyUserAnswers.set(RentalsRaRIncomeCompletePage, true).success.value,
      rentalsAndRentARoomIncomeCYA,
      RentalsRentARoom,
      "income",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(RentalsRaRIncomeCompletePage, false).success.value,
      dummyCall,
      RentalsRentARoom,
      "income",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(ExpensesSectionFinishedPage, true).success.value,
      rentalsExpensesCYA,
      Rentals,
      "expenses",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(ExpensesRRSectionCompletePage, false).success.value,
      dummyCall,
      Rentals,
      "expenses",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(ExpensesRRSectionCompletePage, true).success.value,
      rentARoomExpensesCYA,
      RentARoom,
      "expenses",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(ExpensesRRSectionCompletePage, false).success.value,
      dummyCall,
      RentARoom,
      "expenses",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(RentalsRaRExpensesCompletePage, true).success.value,
      rentalsRentARoomExpensesCYA,
      RentalsRentARoom,
      "expenses",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(RentalsRaRExpensesCompletePage, false).success.value,
      dummyCall,
      RentalsRentARoom,
      "expenses",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(AllowancesSectionFinishedPage, true).success.value,
      rentalsAllowancesCYA,
      Rentals,
      "allowances",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(AllowancesSectionFinishedPage, false).success.value,
      dummyCall,
      Rentals,
      "allowances",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(RaRAllowancesCompletePage, true).success.value,
      rentARoomAllowancesCYA,
      RentARoom,
      "allowances",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(RaRAllowancesCompletePage, false).success.value,
      dummyCall,
      RentARoom,
      "allowances",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(RentalsRaRAllowancesCompletePage, true).success.value,
      rentalsAndRentARoomAllowancesCYA,
      RentalsRentARoom,
      "allowances",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(RentalsRaRAllowancesCompletePage, false).success.value,
      dummyCall,
      RentalsRentARoom,
      "allowances",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(RentalsAdjustmentsCompletePage, true).success.value,
      rentalsAdjustmentsCYA,
      Rentals,
      "adjustments",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(RentalsAdjustmentsCompletePage, false).success.value,
      dummyCall,
      Rentals,
      "adjustments",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(RaRAdjustmentsCompletePage, true).success.value,
      rentARoomAdjustmentsCYA,
      RentARoom,
      "adjustments",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(RaRAdjustmentsCompletePage, false).success.value,
      dummyCall,
      RentARoom,
      "adjustments",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(RentalsRaRAdjustmentsCompletePage, true).success.value,
      rentalsAndRentARoomAdjustmentsCYA,
      RentalsRentARoom,
      "adjustments",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(RentalsRaRAdjustmentsCompletePage, false).success.value,
      dummyCall,
      RentalsRentARoom,
      "adjustments",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(EsbaSectionFinishedPage(Rentals), true).success.value,
      esbasCYA(Rentals),
      Rentals,
      "esba",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(EsbaSectionFinishedPage(Rentals), false).success.value,
      dummyCall,
      Rentals,
      "esba",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(EsbaSectionFinishedPage(RentalsRentARoom), true).success.value,
      esbasCYA(RentalsRentARoom),
      RentalsRentARoom,
      "esba",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(EsbaSectionFinishedPage(RentalsRentARoom), false).success.value,
      dummyCall,
      RentalsRentARoom,
      "esba",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(SbaSectionFinishedPage(Rentals), true).success.value,
      sbasCYA(Rentals),
      Rentals,
      "sba",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(SbaSectionFinishedPage(Rentals), false).success.value,
      dummyCall,
      Rentals,
      "sba",
      description => s"$description for NOT completed"
    ),
    (
      emptyUserAnswers.set(SbaSectionFinishedPage(RentalsRentARoom), true).success.value,
      sbasCYA(RentalsRentARoom),
      RentalsRentARoom,
      "sba",
      description => s"$description for completed"
    ),
    (
      emptyUserAnswers.set(SbaSectionFinishedPage(RentalsRentARoom), false).success.value,
      dummyCall,
      RentalsRentARoom,
      "sba",
      description => s"$description for NOT completed"
    )
  )

  forAll(scenarios) {
    (
      userAnswers: UserAnswers,
      expectedCall: Call,
      propertyType: PropertyType,
      journeyName: String,
      description: String => String
    ) =>
      description(s"$journeyName for ${propertyType.toString}") - {
        "should redirect to right url" in {

          cyaDiversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, journeyName, propertyType) {
            dummyCall
          } mustBe expectedCall

        }
      }
  }
}
