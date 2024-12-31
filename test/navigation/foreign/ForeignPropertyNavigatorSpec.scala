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
import controllers.foreign.expenses.routes._
import controllers.foreign.allowances.routes._
import controllers.foreign.income.routes._
import controllers.foreign.routes._
import controllers.routes.SummaryController
import models.ForeignTotalIncome._
import models.JourneyName.{reads, writes}
import models.{CheckMode, ConsolidatedOrIndividualExpenses, NormalMode, PremiumCalculated, UserAnswers}
import navigation.ForeignPropertyNavigator
import pages.Page
import pages.foreign._
import pages.foreign.allowances._
import pages.foreign.expenses._
import pages.foreign.income._
import play.api.libs.json.Format.GenericFormat

import java.time.LocalDate

class ForeignPropertyNavigatorSpec extends SpecBase {

  private val navigator = new ForeignPropertyNavigator
  private val taxYear = LocalDate.now.getYear
  private val countryCode = "BRA"

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

      "must go from ForeignOtherIncomeFromPropertyPage to CheckYourAnswers Page" in {
        val userAnswers = UserAnswers("test")
        val updatedUserAnswers = userAnswers.set(ForeignOtherIncomeFromPropertyPage("AUS"), BigDecimal(1000.00)).get
        navigator.nextPage(
          ForeignOtherIncomeFromPropertyPage("AUS"),
          taxYear,
          NormalMode,
          userAnswers,
          updatedUserAnswers
        ) mustBe controllers.foreign.income.routes.ForeignIncomeCheckYourAnswersController
          .onPageLoad(taxYear, "AUS")
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

      "CalculatedPremiumLeaseTaxablePage" - {
        "should go to ForeignReceivedGrantLeaseAmount if no selected" in {
          val userAnswersWithData = UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage("ESP"),
              PremiumCalculated(calculatedPremiumLeaseTaxable = false, None)
            )
            .get
          navigator.nextPage(
            CalculatedPremiumLeaseTaxablePage("ESP"),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswersWithData
          ) mustBe ForeignReceivedGrantLeaseAmountController.onPageLoad(taxYear, "ESP", NormalMode)
        }
        "should go from CalculatedPremiumLeaseTaxablePage to Reverse premiums received" in {
          val userAnswersWithData = UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage("ESP"),
              PremiumCalculated(calculatedPremiumLeaseTaxable = true, Some(BigDecimal(1234)))
            )
            .get
          navigator.nextPage(
            CalculatedPremiumLeaseTaxablePage("ESP"),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswersWithData
          ) mustBe ForeignReversePremiumsReceivedController.onPageLoad(taxYear, "ESP", NormalMode)
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

      "must go from PremiumsGrantLeaseYNPage to Have You Calculated Figure Yourself if true" in {
        val countryCode = "BRA"
        navigator.nextPage(
          PremiumsGrantLeaseYNPage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test").set(PremiumsGrantLeaseYNPage(countryCode), value = true).get
        ) mustBe CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from PremiumsGrantLeaseYNPage to Have You Calculated Figure Yourself if false" in {
        val countryCode = "BRA"
        navigator.nextPage(
          PremiumsGrantLeaseYNPage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test").set(PremiumsGrantLeaseYNPage(countryCode), value = true).get
        ) mustBe CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from CalculatedPremiumLeaseTaxablePage to ForeignReversePremiumsReceived if true" in {
        val countryCode = "BRA"
        navigator.nextPage(
          CalculatedPremiumLeaseTaxablePage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage(countryCode),
              PremiumCalculated(calculatedPremiumLeaseTaxable = true, Some(10.00))
            )
            .get
        ) mustBe ForeignReversePremiumsReceivedController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from CalculatedPremiumLeaseTaxablePage to ForeignReceivedGrantLease if false" in {
        val countryCode = "BRA"
        navigator.nextPage(
          CalculatedPremiumLeaseTaxablePage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage(countryCode),
              PremiumCalculated(calculatedPremiumLeaseTaxable = false, None)
            )
            .get
        ) mustBe ForeignReceivedGrantLeaseAmountController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from PremiumsGrantLeaseYNPage to Calculate Premium Lease Taxable if true" in {
        val countryCode = "BRA"
        navigator.nextPage(
          PremiumsGrantLeaseYNPage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test").set(PremiumsGrantLeaseYNPage(countryCode), value = true).get
        ) mustBe CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from PremiumsGrantLeaseYNPage to Reverse Premium Page if false" in {
        val countryCode = "BRA"
        navigator.nextPage(
          PremiumsGrantLeaseYNPage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test").set(PremiumsGrantLeaseYNPage(countryCode), value = false).get
        ) mustBe ForeignReversePremiumsReceivedController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from CalculatedPremiumLeaseTaxablePage to ForeignReversePremiumsReceived if true in NormalMode" in {
        val countryCode = "BRA"
        navigator.nextPage(
          CalculatedPremiumLeaseTaxablePage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage(countryCode),
              PremiumCalculated(calculatedPremiumLeaseTaxable = true, Some(10.00))
            )
            .get
        ) mustBe ForeignReversePremiumsReceivedController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from CalculatedPremiumLeaseTaxablePage to ForeignReceivedGrantLease if false in Normal Mode" in {
        val countryCode = "BRA"
        navigator.nextPage(
          CalculatedPremiumLeaseTaxablePage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage(countryCode),
              PremiumCalculated(calculatedPremiumLeaseTaxable = false, None)
            )
            .get
        ) mustBe ForeignReceivedGrantLeaseAmountController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from ForeignReceivedGrantLeaseAmount to ForeignYearLeaseAmount" in {
        val countryCode = "BRA"
        navigator.nextPage(
          ForeignReceivedGrantLeaseAmountPage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              ForeignReceivedGrantLeaseAmountPage(countryCode),
              BigDecimal(3.0)
            )
            .get
        ) mustBe TwelveMonthPeriodsInLeaseController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from ForeignYearLeaseAmount to ForeignPremiumsGrantLease" in {
        val countryCode = "BRA"
        navigator.nextPage(
          TwelveMonthPeriodsInLeasePage(countryCode),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              TwelveMonthPeriodsInLeasePage(countryCode),
              11
            )
            .get
        ) mustBe ForeignPremiumsGrantLeaseController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "must go from Have You Finished-ForeignIncomeSectionCompletePage to the Summary Page" in {
        val userAnswersWithData = UserAnswers("test").set(ForeignIncomeSectionCompletePage("ESP"), true).get
        navigator.nextPage(
          ForeignIncomeSectionCompletePage("ESP"),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          userAnswersWithData
        ) mustBe SummaryController.show(taxYear)
      }

      "for Expenses section" - {

        "must go from ForeignRentsRatesAndInsurancePage to ForeignPropertyRepairsAndMaintenanceController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignRentsRatesAndInsurancePage(countryCode), BigDecimal(25.00))
            .get
          navigator.nextPage(
            ForeignRentsRatesAndInsurancePage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignPropertyRepairsAndMaintenanceController.onPageLoad(taxYear, countryCode, NormalMode)
        }

        "must go from ForeignPropertyRepairsAndMaintenancePage to ForeignNonResidentialPropertyFinanceCostsController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignPropertyRepairsAndMaintenancePage(countryCode), BigDecimal(650.49))
            .get
          navigator.nextPage(
            ForeignPropertyRepairsAndMaintenancePage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignNonResidentialPropertyFinanceCostsController.onPageLoad(taxYear, countryCode, NormalMode)
        }

        "must go from ForeignNonResidentialPropertyFinanceCostsPage to ForeignProfessionalFeesController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignNonResidentialPropertyFinanceCostsPage(countryCode), BigDecimal(432.00))
            .get
          navigator.nextPage(
            ForeignNonResidentialPropertyFinanceCostsPage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignProfessionalFeesController.onPageLoad(taxYear, countryCode, NormalMode)
        }

        "must go from ForeignProfessionalFeesPage to ForeignCostsOfServicesProvidedController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignProfessionalFeesPage(countryCode), BigDecimal(99.99))
            .get
          navigator.nextPage(
            ForeignProfessionalFeesPage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignCostsOfServicesProvidedController.onPageLoad(taxYear, countryCode, NormalMode)
        }

        "must go from ForeignCostsOfServicesProvidedPage to ForeignOtherAllowablePropertyExpensesController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignCostsOfServicesProvidedPage(countryCode), BigDecimal(432.00))
            .get
          navigator.nextPage(
            ForeignCostsOfServicesProvidedPage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignOtherAllowablePropertyExpensesController.onPageLoad(taxYear, countryCode, NormalMode)
        }

        "must go from ForeignOtherAllowablePropertyExpensesPage to ForeignPropertyExpensesCheckYourAnswersController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignOtherAllowablePropertyExpensesPage(countryCode), BigDecimal(657.00))
            .get
          navigator.nextPage(
            ForeignOtherAllowablePropertyExpensesPage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
        }

        "must go from ConsolidatedOrIndividualExpensesPage to ForeignRentsRatesAndInsuranceController if claiming individual expenses " in {
          val userAnswers = UserAnswers("test")
            .set(
              ConsolidatedOrIndividualExpensesPage(countryCode),
              ConsolidatedOrIndividualExpenses(consolidatedOrIndividualExpensesYesNo = false, None)
            )
            .get
          navigator.nextPage(
            ConsolidatedOrIndividualExpensesPage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignRentsRatesAndInsuranceController.onPageLoad(taxYear, countryCode, NormalMode)
        }

        "must go from ConsolidatedOrIndividualExpensesPage to ForeignPropertyExpensesCheckYourAnswersController if claiming consolidated expenses " in {
          val userAnswers = UserAnswers("test")
            .set(
              ConsolidatedOrIndividualExpensesPage(countryCode),
              ConsolidatedOrIndividualExpenses(
                consolidatedOrIndividualExpensesYesNo = true,
                enteredAmount = Some(BigDecimal(789.00))
              )
            )
            .get
          navigator.nextPage(
            ConsolidatedOrIndividualExpensesPage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
        }

      }

      "for Allowances section" - {

        "must go from ForeignZeroEmissionCarAllowancePage to ForeignZeroEmissionGoodsVehiclesController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignZeroEmissionCarAllowancePage(countryCode), BigDecimal(657.00))
            .get

          navigator.nextPage(
            ForeignZeroEmissionCarAllowancePage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignZeroEmissionGoodsVehiclesController.onPageLoad(taxYear, countryCode, NormalMode)
        }

        "must go from ForeignZeroEmissionGoodsVehiclesPage to ForeignReplacementOfDomesticGoodsController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignZeroEmissionGoodsVehiclesPage(countryCode), BigDecimal(657.00))
            .get

          navigator.nextPage(
            ForeignZeroEmissionGoodsVehiclesPage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignReplacementOfDomesticGoodsController.onPageLoad(taxYear, countryCode, NormalMode)
        }

        "must go from ForeignReplacementOfDomesticGoodsPage to ForeignOtherCapitalAllowancesController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignReplacementOfDomesticGoodsPage(countryCode), BigDecimal(657.00))
            .get

          navigator.nextPage(
            ForeignReplacementOfDomesticGoodsPage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignOtherCapitalAllowancesController.onPageLoad(taxYear, countryCode, NormalMode)
        }

        "must go from ForeignOtherCapitalAllowancesPage to ForeignAllowancesCheckYourAnswersController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignOtherCapitalAllowancesPage(countryCode), BigDecimal(657.00))
            .get

          navigator.nextPage(
            ForeignOtherCapitalAllowancesPage(countryCode),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
        }

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
        ) mustBe CountriesRentedPropertyController.onPageLoad(taxYear, CheckMode)
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

      "must go from TotalIncomePage to SelectIncomeCountry if my previous answer was less than £1,000 and now £1,000 or more" in {

        val userAnswers = UserAnswers("test").set(TotalIncomePage, OneThousandAndMore).get
        navigator.nextPage(
          TotalIncomePage,
          taxYear,
          CheckMode,
          UserAnswers("test").set(TotalIncomePage, LessThanOneThousand).get,
          userAnswers
        ) mustBe SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
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

      "CalculatedPremiumLeaseTaxablePage" - {
        "should go to ForeignReceivedGrantLeaseAmount if no selected" in {
          val userAnswersWithData = UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage("ESP"),
              PremiumCalculated(calculatedPremiumLeaseTaxable = false, None)
            )
            .get
          navigator.nextPage(
            CalculatedPremiumLeaseTaxablePage("ESP"),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            userAnswersWithData
          ) mustBe ForeignReceivedGrantLeaseAmountController.onPageLoad(taxYear, "ESP", CheckMode)
        }
        "should go from CalculatedPremiumLeaseTaxablePage to Reverse premiums received" in {
          val userAnswersWithData = UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage("ESP"),
              PremiumCalculated(calculatedPremiumLeaseTaxable = true, Some(BigDecimal(1234)))
            )
            .get
          navigator.nextPage(
            CalculatedPremiumLeaseTaxablePage("ESP"),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            userAnswersWithData
          ) mustBe ForeignReversePremiumsReceivedController.onPageLoad(taxYear, "ESP", CheckMode)
        }
      }

      "must go from ForeignPropertyRentalIncomePage to CheckYourAnswers" in {
        val countryCode = "BRA"
        navigator.nextPage(
          ForeignPropertyRentalIncomePage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test").set(ForeignPropertyRentalIncomePage(countryCode), BigDecimal(2.3)).get
        ) mustBe ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      }

      "must go from PremiumsGrantLeaseYNPage to Calculate Premium Lease Taxable if true in CheckMode" in {
        val countryCode = "BRA"
        navigator.nextPage(
          PremiumsGrantLeaseYNPage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test").set(PremiumsGrantLeaseYNPage(countryCode), value = true).get
        ) mustBe CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, CheckMode)
      }

      "must go from PremiumsGrantLeaseYNPage to CheckYourAnswers if false in CheckMode" in {
        val countryCode = "BRA"
        navigator.nextPage(
          PremiumsGrantLeaseYNPage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test").set(PremiumsGrantLeaseYNPage(countryCode), value = false).get
        ) mustBe ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      }

      "must go from CalculatedPremiumLeaseTaxablePage to ForeignReversePremiumsReceived if true in CheckMode" in {
        val countryCode = "BRA"
        navigator.nextPage(
          CalculatedPremiumLeaseTaxablePage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage(countryCode),
              PremiumCalculated(calculatedPremiumLeaseTaxable = true, Some(10.00))
            )
            .get
        ) mustBe ForeignReversePremiumsReceivedController.onPageLoad(taxYear, countryCode, CheckMode)
      }

      "must go from CalculatedPremiumLeaseTaxablePage to ForeignReceivedGrantLease if false in CheckMode" in {
        val countryCode = "BRA"
        navigator.nextPage(
          CalculatedPremiumLeaseTaxablePage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              CalculatedPremiumLeaseTaxablePage(countryCode),
              PremiumCalculated(calculatedPremiumLeaseTaxable = false, None)
            )
            .get
        ) mustBe ForeignReceivedGrantLeaseAmountController.onPageLoad(taxYear, countryCode, CheckMode)
      }

      "must go from ForeignReceivedGrantLeaseAmount to ForeignYearLeaseAmount in CheckMode" in {
        val countryCode = "BRA"
        navigator.nextPage(
          ForeignReceivedGrantLeaseAmountPage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              ForeignReceivedGrantLeaseAmountPage(countryCode),
              BigDecimal(3.0)
            )
            .get
        ) mustBe TwelveMonthPeriodsInLeaseController.onPageLoad(taxYear, countryCode, CheckMode)
      }

      "must go from ForeignYearLeaseAmount to ForeignPremiumsGrantLease in CheckMode" in {
        val countryCode = "BRA"
        navigator.nextPage(
          TwelveMonthPeriodsInLeasePage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
            .set(
              TwelveMonthPeriodsInLeasePage(countryCode),
              9
            )
            .get
        ) mustBe ForeignPremiumsGrantLeaseController.onPageLoad(taxYear, countryCode, CheckMode)
      }

      "must go from ForeignRentsRatesAndInsurancePage to ForeignPropertyExpensesCheckYourAnswersController" in {
        val userAnswers = UserAnswers("test")
          .set(ForeignRentsRatesAndInsurancePage(countryCode), BigDecimal(25.00))
          .get
        navigator.nextPage(
          ForeignRentsRatesAndInsurancePage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      }

      "must go from ForeignPropertyRepairsAndMaintenancePage to ForeignPropertyExpensesCheckYourAnswersController" in {
        val userAnswers = UserAnswers("test")
          .set(ForeignPropertyRepairsAndMaintenancePage(countryCode), BigDecimal(650.49))
          .get
        navigator.nextPage(
          ForeignPropertyRepairsAndMaintenancePage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      }

      "must go from ForeignNonResidentialPropertyFinanceCostsPage to ForeignPropertyExpensesCheckYourAnswersController" in {
        val userAnswers = UserAnswers("test")
          .set(ForeignNonResidentialPropertyFinanceCostsPage(countryCode), BigDecimal(432.00))
          .get
        navigator.nextPage(
          ForeignNonResidentialPropertyFinanceCostsPage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      }

      "must go from ForeignProfessionalFeesPage to ForeignPropertyExpensesCheckYourAnswersController" in {
        val userAnswers = UserAnswers("test")
          .set(ForeignProfessionalFeesPage(countryCode), BigDecimal(99.99))
          .get
        navigator.nextPage(
          ForeignProfessionalFeesPage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      }

      "must go from ForeignCostsOfServicesProvidedPage to ForeignPropertyExpensesCheckYourAnswersController" in {
        val userAnswers = UserAnswers("test")
          .set(ForeignCostsOfServicesProvidedPage(countryCode), BigDecimal(432.00))
          .get
        navigator.nextPage(
          ForeignCostsOfServicesProvidedPage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      }

      "must go from ForeignOtherAllowablePropertyExpensesPage to ForeignPropertyExpensesCheckYourAnswersController" in {
        val userAnswers = UserAnswers("test")
          .set(ForeignOtherAllowablePropertyExpensesPage(countryCode), BigDecimal(657.00))
          .get
        navigator.nextPage(
          ForeignOtherAllowablePropertyExpensesPage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      }

      "must go from ConsolidatedOrIndividualExpensesPage to ForeignPropertyExpensesCheckYourAnswersController" in {
        val userAnswers = UserAnswers("test")
          .set(
            ConsolidatedOrIndividualExpensesPage(countryCode),
            ConsolidatedOrIndividualExpenses(
              consolidatedOrIndividualExpensesYesNo = true,
              enteredAmount = Some(BigDecimal(789.00))
            )
          )
          .get
        navigator.nextPage(
          ConsolidatedOrIndividualExpensesPage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      }

      "must go from ConsolidatedOrIndividualExpensesPage to ForeignRentsRatesAndInsurancePage" in {
        val userAnswers = UserAnswers("test")
          .set(
            ConsolidatedOrIndividualExpensesPage(countryCode),
            ConsolidatedOrIndividualExpenses(consolidatedOrIndividualExpensesYesNo = false, None)
          )
          .get
        navigator.nextPage(
          ConsolidatedOrIndividualExpensesPage(countryCode),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          userAnswers
        ) mustBe ForeignRentsRatesAndInsuranceController.onPageLoad(taxYear, countryCode, NormalMode)
      }

      "for Allowances section check mode" - {

        "must go from ForeignZeroEmissionCarAllowancePage to ForeignAllowancesCheckYourAnswersController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignZeroEmissionCarAllowancePage(countryCode), BigDecimal(657.00))
            .get

          navigator.nextPage(
            ForeignZeroEmissionCarAllowancePage(countryCode),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
        }

        "must go from ForeignZeroEmissionGoodsVehiclesPage to ForeignAllowancesCheckYourAnswersController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignZeroEmissionGoodsVehiclesPage(countryCode), BigDecimal(657.00))
            .get

          navigator.nextPage(
            ForeignZeroEmissionGoodsVehiclesPage(countryCode),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
        }

        "must go from ForeignReplacementOfDomesticGoodsPage to ForeignAllowancesCheckYourAnswersController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignReplacementOfDomesticGoodsPage(countryCode), BigDecimal(657.00))
            .get

          navigator.nextPage(
            ForeignReplacementOfDomesticGoodsPage(countryCode),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
        }

        "must go from ForeignOtherCapitalAllowancesPage to ForeignAllowancesCheckYourAnswersController" in {
          val userAnswers = UserAnswers("test")
            .set(ForeignOtherCapitalAllowancesPage(countryCode), BigDecimal(657.00))
            .get

          navigator.nextPage(
            ForeignOtherCapitalAllowancesPage(countryCode),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
        }

      }

    }
  }
}
