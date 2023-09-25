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

package navigation

import base.SpecBase
import controllers.routes
import pages._
import models._
import pages.propertyrentals.{ClaimPropertyIncomeAllowancePage, ExpensesLessThan1000Page, IsNonUKLandlordPage}

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator
  private val taxYear = LocalDate.now.getYear

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, taxYear, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "must go from UKPropertyDetailsPage to Total Income" in {
        navigator.nextPage(
          UKPropertyDetailsPage, taxYear, NormalMode, UserAnswers("test")
        ) mustBe routes.TotalIncomeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from TotalIncomePage to the UK property select page" in {
        navigator.nextPage(
          TotalIncomePage, taxYear, NormalMode, UserAnswers("test")
        ) mustBe routes.UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      }

      "most go from UKPropertySelectPage to the summary page" in {
        navigator.nextPage(
          UKPropertySelectPage, taxYear, NormalMode, UserAnswers("test")
        ) mustBe routes.SummaryController.show(taxYear)
      }

      "must go from UKPropertyPage to Check Your Answers" in {
        navigator.nextPage(
          UKPropertyPage, taxYear, NormalMode, UserAnswers("test")
        ) mustBe routes.CheckYourAnswersController.onPageLoad
      }

      "must go from LeasePremiumPaymentPage to CalculateFigureYourselfPage" in {
        navigator.nextPage(
          ExpensesLessThan1000Page, taxYear, NormalMode, UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
      }

      "must go from CalculatedFigureYourselfPage to RecievedGrantLeaseAmountPage" in {
        navigator.nextPage(
          CalculatedFigureYourselfPage, taxYear, NormalMode, UserAnswers("test")
        ) mustBe controllers.premiumlease.routes.RecievedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode)
      }

      "must go from RecievedGrantLeaseAmountPage to YearLeaseAmountPage" in {
        navigator.nextPage(
          premiumlease.RecievedGrantLeaseAmountPage, taxYear, NormalMode, UserAnswers("test")
        ) mustBe controllers.premiumlease.routes.YearLeaseAmountController.onPageLoad(taxYear, NormalMode)
      }

      "must go from YearLeaseAmountPage to PremiumsGrantLeasePage" in {
        navigator.nextPage(
          premiumlease.YearLeaseAmountPage, taxYear, NormalMode, UserAnswers("test")
        ) mustBe controllers.premiumlease.routes.PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode)
      }

      "must go from ExpensesLessThan1000Page to ClaimPropertyIncomeAllowancePage" in {
        navigator.nextPage(
          ExpensesLessThan1000Page, taxYear, NormalMode, UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
      }

      "must go from ClaimPropertyIncomeAllowancePage to CheckYourAnswersPage" in {
        navigator.nextPage(
          ClaimPropertyIncomeAllowancePage, taxYear, NormalMode, UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from DeductingTax to IncomeFromPropertyRentalsPage" in {
        navigator.nextPage(
          DeductingTaxPage, taxYear, NormalMode, UserAnswers("test")
        ) mustBe controllers.routes.IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
      }

      "must go from IsNonUKLandlordPage to DeductingTaxPage when answer is yes" in {
        val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage, true).get
        navigator.nextPage(
          IsNonUKLandlordPage, taxYear, NormalMode, userAnswers
        ) mustBe controllers.routes.DeductingTaxController.onPageLoad(taxYear, NormalMode)
      }

      "must go from IsNonUKLandlordPage to IncomeFromPropertyRentalsPage when answer is no" in {
        val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage, false).get
        navigator.nextPage(
          IsNonUKLandlordPage, taxYear, NormalMode, userAnswers
        ) mustBe controllers.routes.IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
      }
    }

    "in Check mode" - {

      "must go from ExpensesLessThan1000Page to CheckYourAnswersPage" in {
        navigator.nextPage(
          ExpensesLessThan1000Page, taxYear, CheckMode, UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ClaimPropertyIncomeAllowancePage to CheckYourAnswersPage" in {
        navigator.nextPage(
          ClaimPropertyIncomeAllowancePage, taxYear, CheckMode, UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, taxYear, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad
      }
    }
  }
}
