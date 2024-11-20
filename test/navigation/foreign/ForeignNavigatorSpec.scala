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

package navigation.foreign

import base.SpecBase
import controllers.foreign.routes._
import controllers.propertyrentals.income.routes.OtherPropertyRentalIncomeController
import models.ForeignTotalIncome._
import models.JourneyName.{reads, writes}
import models.{CheckMode, NormalMode, PremiumCalculated, Rentals, UserAnswers}
import navigation.ForeignPropertyNavigator
import pages.Page
import pages.foreign.income.ForeignPropertyRentalIncomePage
import pages.foreign._
import play.api.libs.json.Format.GenericFormat

import java.time.LocalDate

class ForeignNavigatorSpec extends SpecBase {

  private val navigator = new ForeignPropertyNavigator
  private val taxYear = LocalDate.now.getYear

  "ForeignPropertyNavigator" - {

    "in Normal mode" - {

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

      "must go from TotalIncomePage to PropertyIncomeReportPage if income is less than £1,000" in {
        val userAnswers = UserAnswers("test").set(TotalIncomePage, LessThanOneThousand).get

        navigator.nextPage(
          TotalIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe PropertyIncomeReportController.onPageLoad(taxYear, NormalMode)
      }

      "must go from PropertyIncomeReportPage to ForeignCountriesCheckYourAnswersPage if I don't want to report my property income" in {
        val userAnswers = UserAnswers("test").set(TotalIncomePage, LessThanOneThousand).get
        val updateAnswers = userAnswers.set(PropertyIncomeReportPage, false).get

        navigator.nextPage(
          PropertyIncomeReportPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          updateAnswers
        ) mustBe ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from PropertyIncomeReportPage to ForeignCountriesCheckYourAnswersPage if I  want to report my property income" in {
        val userAnswers = UserAnswers("test").set(TotalIncomePage, LessThanOneThousand).get
        val updateAnswers = userAnswers.set(PropertyIncomeReportPage, true).get

        navigator.nextPage(
          PropertyIncomeReportPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          updateAnswers
        ) mustBe SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
      }

      "must go from TotalIncomePage to SelectIncomeCountryPage if income is more than £1,000" in {
        val userAnswers = UserAnswers("test").set(TotalIncomePage, OneThousandAndMore).get

        navigator.nextPage(
          TotalIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
      }

      "must go from SelectIncomeCountryPage to CountriesRentedPropertyPage" in {
        navigator.nextPage(
          SelectIncomeCountryPage(0),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      }

      "must go from AddCountriesRentedPage to SelectIncomeCountryPage if AddCountriesRentedPage is true" in {
        val userAnswersWithAddCountry = UserAnswers("test").set(AddCountriesRentedPage, true).get

        val updatedUserAnswers = userAnswersWithAddCountry.set(SelectIncomeCountryPage(0), Country("Spain", "ESP")).get
        navigator.nextPage(
          AddCountriesRentedPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          updatedUserAnswers
        ) mustBe SelectIncomeCountryController.onPageLoad(taxYear, 1, NormalMode)
      }

      "must go from AddCountriesRentedPage to ClaimPropertyIncomeAllowanceOrExpensesPage if AddCountriesRentedPage is false" in {
        val userAnswersWithoutAddCountry = UserAnswers("test").set(AddCountriesRentedPage, false).get

        navigator.nextPage(
          AddCountriesRentedPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          userAnswersWithoutAddCountry
        ) mustBe ClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode)
      }

      "must go from ClaimPropertyIncomeAllowanceOrExpensesPage to ForeignCountriesCheckYourAnswersPage" in {
        navigator.nextPage(
          ClaimPropertyIncomeAllowanceOrExpensesPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
      }
      "CalculatedPremiumLEaseTaxablePage" - {
        "should go to ForeignReceivedGrantLeaseAmount if no selected" in {
          val userAnswersWithData = UserAnswers("test").set(
              CalculatedPremiumLeaseTaxablePage("ESP"),
            PremiumCalculated(
              premiumCalculatedYesNo = false,
              None)
          ).get
          navigator.nextPage(
            CalculatedPremiumLeaseTaxablePage("ESP"),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswersWithData
          ) mustBe ForeignReceivedGrantLeaseAmountController.onPageLoad(taxYear, "ESP", NormalMode)
        }
        "should go to OtherPropertyRentalIncomeController if yes selected" in {
          val userAnswersWithData = UserAnswers("test").set(
            CalculatedPremiumLeaseTaxablePage("ESP"),
            PremiumCalculated(
              premiumCalculatedYesNo = true,
              Some(BigDecimal(1234)))
          ).get
          navigator.nextPage(
            CalculatedPremiumLeaseTaxablePage("ESP"),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswersWithData
          ) mustBe OtherPropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, Rentals)
        }
      }

      "must go from ForeignPropertyRentalIncomePage to PremiumsGrantLeaseYNPage" in {
        val countryCode = "BRA"
        navigator.nextPage(
          ForeignPropertyRentalIncomePage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test").set(ForeignPropertyRentalIncomePage(countryCode), BigDecimal(2.3)).get
        ) mustBe PremiumsGrantLeaseYNController.onPageLoad(taxYear, countryCode, NormalMode)
      }

    }

    "in Check mode" - {

      "must go from SelectIncomeCountryPage to ForeignCountriesCheckYourAnswersController in CheckMode" in {
        navigator.nextPage(
          SelectIncomeCountryPage(0),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      }

      "must go from AddCountriesRentedPage to ForeignCountriesCheckYourAnswersController if AddCountriesRentedPage is false" in {
        val userAnswersWithoutAddCountry = UserAnswers("test").set(AddCountriesRentedPage, false).get

        navigator.nextPage(
          AddCountriesRentedPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswersWithoutAddCountry
        ) mustBe ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ClaimPropertyIncomeAllowanceOrExpensesPage to ForeignCountriesCheckYourAnswersController" in {
        val userAnswersWithoutAddCountry = UserAnswers("test").set(AddCountriesRentedPage, false).get

        navigator.nextPage(
          ClaimPropertyIncomeAllowanceOrExpensesPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswersWithoutAddCountry
        ) mustBe ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from DoYouWantToRemoveCountryPage to SelectIncomeCountry if I want to remove the last income country" in {

        val userAnswers = UserAnswers("test").set(DoYouWantToRemoveCountryPage, true).get
        navigator.nextPage(
          DoYouWantToRemoveCountryPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
      }

      "must go from DoYouWantToRemoveCountryPage to CountriesRentedPropertyPage if I want to remove a country" in {

        val userAnswers = UserAnswers("test").set(DoYouWantToRemoveCountryPage, true).get
        val updatedAnswers =
          userAnswers.set(IncomeSourceCountries, Array(Country("Brazil", "BRA"), Country("Japan", "JPN"))).get
        navigator.nextPage(
          DoYouWantToRemoveCountryPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          updatedAnswers
        ) mustBe CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      }

      "must go from DoYouWantToRemoveCountryPage to CountriesRentedPropertyPage if I don't want to remove a country" in {

        val userAnswers = UserAnswers("test").set(DoYouWantToRemoveCountryPage, false).get
        navigator.nextPage(
          DoYouWantToRemoveCountryPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      }

      "must go from a page that doesn't exist in the route map to Index in CheckMode" in {
        case object UnknownPage extends Page

        navigator.nextPage(
          UnknownPage,
          taxYear,
          CheckMode,
          UserAnswers("id"),
          UserAnswers("id")
        ) mustBe controllers.routes.IndexController.onPageLoad
      }
    }
  }

}
