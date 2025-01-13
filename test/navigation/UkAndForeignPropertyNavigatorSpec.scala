/*
 * Copyright 2025 HM Revenue & Customs
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
import controllers.ukandforeignproperty.routes
import models._
import pages.ukandforeignproperty._
import pages.foreign.Country
import pages.{Page, UkAndForeignPropertyRentalTypeUkPage}

import java.time.LocalDate

class UkAndForeignPropertyNavigatorSpec  extends SpecBase {

  private val navigator = new UkAndForeignPropertyNavigator
  private val taxYear = LocalDate.now.getYear

  "UkAndForeignPropertyNavigator" - {

    "must go from a page that doesn't exist in the route map to Index" in {
      case object UnknownPage extends Page

      navigator.nextPage(
        UnknownPage,
        taxYear,
        NormalMode,
        UserAnswers("id"),
        UserAnswers("id")
      ) mustBe controllers.routes.IndexController.onPageLoad
    }

    "Total Property Income in Normal mode" - {

      "must go from a TotalPropertyIncomePage to ReportIncomePage when the option selected is 'less then £1000'" in {

        val ua = UserAnswers("id")
          .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan).success.value

        navigator.nextPage(
          TotalPropertyIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          ua
        ) mustBe routes.ReportIncomeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from a TotalPropertyIncomePage to UkAndForeignPropertyRentalTypeUkPage when the option selected is '£1000 or more'" in {

        val ua = UserAnswers("id")
          .set(TotalPropertyIncomePage, TotalPropertyIncome.Maximum).success.value

        navigator.nextPage(
          TotalPropertyIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          ua
        ) mustBe routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear = taxYear, mode = NormalMode)
      }
    }

    "Uk And Foreign Property Rental Type Uk" - {
      val testCountry: Country = Country("Greece", "GRC")
      val userAnswersWith0Country = emptyUserAnswers
      val userAnswersWith1Country = emptyUserAnswers.set(SelectCountryPage, List(testCountry)).success.value

      "in Normal mode" - {
        "must go to SelectCountryPage" in {
          navigator.nextPage(
            UkAndForeignPropertyRentalTypeUkPage,
            taxYear,
            NormalMode,
            UserAnswers("id"),
            userAnswersWith0Country
          ) mustBe routes.SelectCountryController.onPageLoad(taxYear, Index(1), NormalMode)
        }
        "must go to ForeignCountriesRented when at least one counter" in {
          navigator.nextPage(
            UkAndForeignPropertyRentalTypeUkPage,
            taxYear,
            NormalMode,
            UserAnswers("id"),
            userAnswersWith1Country
          ) mustBe routes.ForeignCountriesRentedController.onPageLoad(taxYear, NormalMode)
        }
      }

      "in Check mode" - {
        "must go to the CYA page" ignore {
          navigator.nextPage(
            UkAndForeignPropertyRentalTypeUkPage,
            taxYear,
            CheckMode,
            UserAnswers("id"),
            UserAnswers("id")
          ) mustBe controllers.routes.IndexController.onPageLoad //TODO - update to CYA route when it exists
        }
      }

    }

    "Foreign Countries Rented in Normal mode" - {

      "must go from AddCountriesRentedPage to  if AddCountriesRentedPage is true" in {
        val userAnswersWithAddCountry = UserAnswers("id").set(ForeignCountriesRentedPage, true).get

        navigator.nextIndex(
          ForeignCountriesRentedPage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          userAnswersWithAddCountry,
          1
        ) mustBe
          routes.SelectCountryController.onPageLoad(taxYear, Index(2), NormalMode)
      }

      "must go from AddCountriesRentedPage to next page(ClaimIncomeAndExpensesPage) if AddCountriesRentedPage is false" ignore {
        val userAnswersWithAddCountry = UserAnswers("id").set(ForeignCountriesRentedPage, false).get

        navigator.nextPage(
          ForeignCountriesRentedPage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          userAnswersWithAddCountry
        ) mustBe ???
      }
    }

    "Claim expenses or rent a room relief in Normal mode" - {
      "must go to Claim property income allowance or expenses Page" - {
        "when the option selected is 'Rent a room relief'" in {
          navigator.nextIndex(
            UkAndForeignPropertyClaimExpensesOrReliefPage,
            taxYear,
            NormalMode,
            emptyUserAnswers,
            emptyUserAnswers.set(UkAndForeignPropertyClaimExpensesOrReliefPage, UkAndForeignPropertyClaimExpensesOrRelief(true)).get,
            0
          ) mustBe controllers.ukandforeignproperty.routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear,NormalMode)
        }
        "when the option selected is 'Expenses'" in {
          navigator.nextIndex(
            UkAndForeignPropertyClaimExpensesOrReliefPage,
            taxYear,
            NormalMode,
            emptyUserAnswers,
            emptyUserAnswers.set(
              UkAndForeignPropertyClaimExpensesOrReliefPage,
              UkAndForeignPropertyClaimExpensesOrRelief(false)
            ).get,
            0
          ) mustBe controllers.ukandforeignproperty.routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear,NormalMode)
        }
      }
    }

    "Claim property income allowance or expenses in Normal mode" - {
      "must go to 'How much income did you get from your foreign property rentals'" - {
        "when the option selected is 'Use the property income allowance' " ignore {
          navigator.nextIndex(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            taxYear,
            NormalMode,
            emptyUserAnswers,
            emptyUserAnswers
              .set(
                UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true)
              )
              .get,
            0
          ) mustBe ??? //TODO
        }

        "when the option selected is 'Expenses' " ignore {
          navigator.nextIndex(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            taxYear,
            NormalMode,
            emptyUserAnswers,
            emptyUserAnswers
              .set(
                UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(false)
              )
              .get,
            0
          ) mustBe ??? //TODO
        }
      }

      "must go to 'Non-UK resident landlord'" - {
        "when the option selected is 'Use the property income allowance' and UkAndForeignPropertyRentalTypeUkPage option is'Property rentals'" in {
          val userAnswers = UserAnswers("id")
            .set(UkAndForeignPropertyRentalTypeUkPage, Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.PropertyRentals)).success.value
            .set(
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
              UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true)
            ).success.value

          navigator.nextIndex(
            UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
            taxYear,
            NormalMode,
            emptyUserAnswers,
            userAnswers,
            0
          ) mustBe routes.NonResidentLandlordUKController.onPageLoad(taxYear, NormalMode)
        }
      }
    }

    "Non-UK resident landlord" - {

      "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' when the option 'true' is selected" ignore {
        val nonResidentLandlordUKPage = UserAnswers("id").set(NonResidentLandlordUKPage, true).get

        navigator.nextIndex(
          NonResidentLandlordUKPage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          nonResidentLandlordUKPage,
          1
        ) mustBe ???
      }

      "must go from NonResidentLandlordUKPage to 'How much income did you get from your foreign property rentals?' when the option 'false' is selected" ignore {
        val nonResidentLandlordUKPage = UserAnswers("id").set(NonResidentLandlordUKPage, false).get

        navigator.nextPage(
          NonResidentLandlordUKPage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          nonResidentLandlordUKPage
        ) mustBe ???
      }

      "must go from NonResidentLandlordUKPage to 'CYA page' when in CheckMode" ignore {
        val nonResidentLandlordUKPage = UserAnswers("id").set(NonResidentLandlordUKPage, false).get

        navigator.nextPage(
          NonResidentLandlordUKPage,
          taxYear,
          CheckMode,
          UserAnswers("id"),
          nonResidentLandlordUKPage
        ) mustBe ???
      }
    }

    "Total Property Income in Check mode" - {

      "must go from a TotalPropertyIncomePage to ReportIncomePage when the option selected is 'less than £1000'" in {
        val previousAnswers = UserAnswers("id")
          .set(TotalPropertyIncomePage, TotalPropertyIncome.Maximum).success.value
        val userAnswers = UserAnswers("id")
          .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan).success.value

        navigator.nextPage(
          TotalPropertyIncomePage,
          taxYear,
          CheckMode,
          previousAnswers,
          userAnswers
        ) mustBe routes.ReportIncomeController.onPageLoad(taxYear, CheckMode)
      }

      "must go from a TotalPropertyIncomePage to UkAndForeignPropertyRentalTypeUkPage when the option selected is '£1000 or more'" in {
        val previousAnswers = UserAnswers("id")
          .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan).success.value
        val userAnswers = UserAnswers("id")
          .set(TotalPropertyIncomePage, TotalPropertyIncome.Maximum).success.value

        navigator.nextPage(
          TotalPropertyIncomePage,
          taxYear,
          CheckMode,
          previousAnswers,
          userAnswers
        ) mustBe routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, NormalMode)
      }

      "must redirect to CYA when answer remains the same" in {
        val ua = UserAnswers("id")
          .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan).success.value

        navigator.nextPage(
          TotalPropertyIncomePage,
          taxYear,
          CheckMode,
          UserAnswers("id"),
          ua
        ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
      }
    }

    "Report Income in Check mode" - {

      "must go from a ReportIncomePage to UkAndForeignPropertyRentalTypeUkPage when the option selected is 'less than £1000'" in {
        val previousAnswers = UserAnswers("id")
          .set(ReportIncomePage, ReportIncome.DoNoWantToReport).success.value
        val userAnswers = UserAnswers("id")
          .set(ReportIncomePage, ReportIncome.WantToReport).success.value

        navigator.nextPage(
          ReportIncomePage,
          taxYear,
          CheckMode,
          previousAnswers,
          userAnswers
        ) mustBe routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, NormalMode)
      }

      "must go from a ReportIncomePage to Uk And foreign property CYA when answer remains the same" in {
        val ua = UserAnswers("id")
          .set(ReportIncomePage, ReportIncome.DoNoWantToReport).success.value

        navigator.nextPage(
          ReportIncomePage,
          taxYear,
          CheckMode,
          UserAnswers("id"),
          ua
        ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
      }
    }
  }
}
