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

package navigation

import base.SpecBase
import controllers.ukandforeignproperty.routes
import models.{CheckMode, Index, NormalMode, TotalPropertyIncome, UserAnswers}
import pages.ukandforeignproperty.{ForeignCountriesRentedPage, NonResidentLandlordUKPage, TotalPropertyIncomePage}
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
      "in Normal mode" - {
        "must go to SelectCountryPage" in {
          navigator.nextPage(
            UkAndForeignPropertyRentalTypeUkPage,
            taxYear,
            NormalMode,
            UserAnswers("id"),
            UserAnswers("id")
          ) mustBe routes.SelectCountryController.onPageLoad(taxYear, Index(1), NormalMode)
        }
      }

      "in Check mode" - {
        "must go to the CYA page" in {
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
  }
}