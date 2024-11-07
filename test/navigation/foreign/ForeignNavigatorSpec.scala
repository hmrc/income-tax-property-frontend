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
import controllers.foreign.routes
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.ForeignPropertyNavigator
import pages.Page
import pages.foreign.{AddCountriesRentedPage, Country,SelectIncomeCountryPage}

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

      "must go from SelectIncomeCountryPage to CountriesRentedPropertyController" in {
        navigator.nextPage(
          SelectIncomeCountryPage(0),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe routes.CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      }

      "must go from AddCountriesRentedPage to SelectIncomeCountryController if AddCountriesRentedPage is true" in {
        val userAnswersWithAddCountry = UserAnswers("test").set(AddCountriesRentedPage, true).get

        val updatedUserAnswers = userAnswersWithAddCountry.set(SelectIncomeCountryPage(0), Country("Spain", "ESP")).get
        navigator.nextPage(
          AddCountriesRentedPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          updatedUserAnswers
        ) mustBe routes.SelectIncomeCountryController.onPageLoad(taxYear, 1, NormalMode)
      }

      "must go from AddCountriesRentedPage to CountriesRentedPropertyController if AddCountriesRentedPage is false" in {
        val userAnswersWithoutAddCountry = UserAnswers("test").set(AddCountriesRentedPage, false).get

        navigator.nextPage(
          AddCountriesRentedPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          userAnswersWithoutAddCountry
        ) mustBe routes.CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      }
    }

    "in Check mode" - {

      "must go from SelectIncomeCountryPage to CountriesRentedPropertyController in CheckMode" in {
        navigator.nextPage(
          SelectIncomeCountryPage(0),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe routes.CountriesRentedPropertyController.onPageLoad(taxYear, CheckMode)
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
