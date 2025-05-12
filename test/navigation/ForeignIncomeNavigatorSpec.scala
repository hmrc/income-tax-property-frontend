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
import controllers.foreignincome.dividends.routes._
import models.{CheckMode, NormalMode, UserAnswers}
import pages.foreign.Country
import pages.foreignincome.{CountryReceiveDividendIncomePage, DividendIncomeSourceCountries, IncomeBeforeForeignTaxDeductedPage}
import pages.foreignincome.dividends._
import service.ForeignIncomeCYADiversionService

import java.time.LocalDate

class ForeignIncomeNavigatorSpec extends SpecBase {
  val navigator = new ForeignIncomeNavigator(new ForeignIncomeCYADiversionService())
  private val taxYear = LocalDate.now.getYear
  private val country: Country = Country("Spain", "ESP")
  private val index = 0

  "ForeignIncomeNavigator" - {
    "in Normal mode" - {
      "must go from the 'Foreign Dividends Income Country' page to the 'Dividend Income Before Foreign Tax Deducted' page" in {
        val ua = UserAnswers("test")
          .set(CountryReceiveDividendIncomePage(index), country)
          .get
        navigator.nextPage(
          CountryReceiveDividendIncomePage(index),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          ua
        ) mustBe IncomeBeforeForeignTaxDeductedController.onPageLoad(taxYear, country.code, NormalMode)
      }
      "must go from the 'Dividend Income Before Foreign Tax' page to the 'Foreign Tax Deducted From Dividend Income' page" in {
        navigator.nextPage(
          IncomeBeforeForeignTaxDeductedPage(country.code),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe ForeignTaxDeductedFromDividendIncomeController.onPageLoad(taxYear, country.code, NormalMode)
      }
      "must go from the 'Foreign Tax Deducted From Dividend Income' page to the 'How much Foreign Tax was Deducted' page " +
        "when the user selects 'Yes'" in {
        val ua = UserAnswers("test")
          .set(ForeignTaxDeductedFromDividendIncomePage(country.code), true)
          .get
        navigator.nextPage(
          ForeignTaxDeductedFromDividendIncomePage(country.code),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          ua
        ) mustBe HowMuchForeignTaxDeductedFromDividendIncomeController.onPageLoad(taxYear, country.code, NormalMode)
      }
      "must go from the 'Foreign Tax Deducted From Dividend Income' page to the 'Dividends CYA' page " +
        "when the user selects 'No'" in {
        val ua = UserAnswers("test")
          .set(ForeignTaxDeductedFromDividendIncomePage(country.code), false)
          .get
        navigator.nextPage(
          ForeignTaxDeductedFromDividendIncomePage(country.code),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          ua
        ) mustBe DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, country.code)
      }
      "must go from the 'How much Foreign Tax was Deducted' page to the 'Claim Foreign Tax Credit Relief' page" in {
        navigator.nextPage(
          HowMuchForeignTaxDeductedFromDividendIncomePage(country.code),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe ClaimForeignTaxCreditReliefController.onPageLoad(taxYear, country.code, NormalMode)
      }
      "must go from the 'Claim Foreign Tax Credit Relief' page to the 'Dividends CYA' page" in {
        navigator.nextPage(
          ClaimForeignTaxCreditReliefPage(country.code),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, country.code)

      }
      "must go from the 'Your Foreign Dividends by Country' page to the 'Foreign Dividends Income Country' page " +
        "when the user selects 'Yes'" in {
        val ua = UserAnswers("test")
          .set(YourForeignDividendsByCountryPage, true)
          .get

        navigator.nextPage(
          YourForeignDividendsByCountryPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          ua
        ) mustBe CountryReceiveDividendIncomeController.onPageLoad(taxYear, index, NormalMode)
      }
      "must go from the 'Your Foreign Dividends by Country' page to the 'Dividends Section Complete' page " +
        "when the user selects 'No'" in {
        val ua = UserAnswers("test")
          .set(YourForeignDividendsByCountryPage, false)
          .get

        navigator.nextPage(
          YourForeignDividendsByCountryPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          ua
        ) mustBe DividendsSectionFinishedController.onPageLoad(taxYear)
      }
      "must go from the 'Remove Foreign Dividend Income' page to 'Foreign Dividends Income Country' " +
        "when the user selects yes to remove their only Dividend Income" in {
        val ua = UserAnswers("test")
          .set(RemoveForeignDividendPage, true)
          .get

        navigator.nextPage(
          RemoveForeignDividendPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          ua
        ) mustBe CountryReceiveDividendIncomeController.onPageLoad(taxYear, 0, NormalMode)
      }
      "must go from the 'Remove Foreign Dividend Income' page to 'Your Foreign Dividends by Country' " +
      "when the user selects yes to remove one of their Dividend Incomes" in {
        val country: Country = Country("Spain", "ESP")
        val ua = UserAnswers("test")
          .set(RemoveForeignDividendPage, true)
          .flatMap(_.set(DividendIncomeSourceCountries, Array(country)))
          .get

        navigator.nextPage(
          RemoveForeignDividendPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          ua
        ) mustBe YourForeignDividendsByCountryController.onPageLoad(taxYear)
      }
      "must go from the 'Remove Foreign Dividend Income' page to 'Your Foreign Dividends by Country' " +
        "when the user selects no" in {
        val ua = UserAnswers("test")
          .set(RemoveForeignDividendPage, false)
          .get

        navigator.nextPage(
          RemoveForeignDividendPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          ua
        ) mustBe YourForeignDividendsByCountryController.onPageLoad(taxYear)
      }

    }
    "in Check mode" - {
      "must go from the 'Foreign Dividends Income Country' page to the 'Dividend Income Before Foreign Tax Deducted' page" in {
        val ua = UserAnswers("test")
          .set(CountryReceiveDividendIncomePage(index), country)
          .get
        navigator.nextPage(
          CountryReceiveDividendIncomePage(index),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          ua
        ) mustBe IncomeBeforeForeignTaxDeductedController.onPageLoad(taxYear, country.code, NormalMode)
      }
      "must go from the 'Dividend Income Before Foreign Tax' page to the 'Dividends CYA' page" in {
        navigator.nextPage(
          IncomeBeforeForeignTaxDeductedPage(country.code),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, country.code)
      }
      "must go from the 'Foreign Tax Deducted From Dividend Income' page to the 'Dividends CYA' page " +
        "when the user selects 'No'" in {
        val ua = UserAnswers("test")
          .set(ForeignTaxDeductedFromDividendIncomePage(country.code), false)
          .get
        navigator.nextPage(
          ForeignTaxDeductedFromDividendIncomePage(country.code),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          ua
        ) mustBe DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, country.code)
      }
      "must go from the 'Foreign Tax Deducted From Dividend Income' page to the 'Dividends CYA' page " +
        "when the user doesn't change their answer" in {
        val prevUa = UserAnswers("test")
          .set(ForeignTaxDeductedFromDividendIncomePage(country.code), true)
          .get
        val ua = UserAnswers("test")
          .set(ForeignTaxDeductedFromDividendIncomePage(country.code), true)
          .get
        navigator.nextPage(
          ForeignTaxDeductedFromDividendIncomePage(country.code),
          taxYear,
          CheckMode,
          prevUa,
          ua
        ) mustBe DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, country.code)
      }
      "must go from the 'Foreign Tax Deducted From Dividend Income' page to the 'How much Foreign Tax was Deducted' page " +
        "when the user changes their answer from 'No' to'Yes'" in {
        val prevUa = UserAnswers("test")
          .set(ForeignTaxDeductedFromDividendIncomePage(country.code), false)
          .get
        val ua = UserAnswers("test")
          .set(ForeignTaxDeductedFromDividendIncomePage(country.code), true)
          .get
        navigator.nextPage(
          ForeignTaxDeductedFromDividendIncomePage(country.code),
          taxYear,
          CheckMode,
          prevUa,
          ua
        ) mustBe HowMuchForeignTaxDeductedFromDividendIncomeController.onPageLoad(taxYear, country.code, NormalMode)
      }
      "must go from the 'How much Foreign Tax was Deducted' page to the 'Dividends CYA' page" in {
        navigator.nextPage(
          HowMuchForeignTaxDeductedFromDividendIncomePage(country.code),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, country.code)
      }
      "must go from the 'Claim Foreign Tax Credit Relief' page to the 'Dividends CYA' page" in {
        navigator.nextPage(
          ClaimForeignTaxCreditReliefPage(country.code),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, country.code)

      }
    }
  }
}
