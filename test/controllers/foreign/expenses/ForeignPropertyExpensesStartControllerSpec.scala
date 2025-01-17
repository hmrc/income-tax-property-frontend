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

package controllers.foreign.expenses

import base.SpecBase
import models.UserAnswers
import pages.foreign.{Country, IncomeSourceCountries}
import pages.foreign.income.ForeignPropertyRentalIncomePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.foreign.expenses.ForeignPropertyExpensesStartView

class ForeignPropertyExpensesStartControllerSpec extends SpecBase {

  val taxYear = 2024
  val countryCode = "AUS"
  val countryName = "Australia"
  val isIncomeUnder85k = false

  "ForeignPropertyExpensesStart Controller" - {

    "must return OK and the correct view for a GET if income under 85k" in {

      val userAnswers: UserAnswers =
        UserAnswers("test").set(ForeignPropertyRentalIncomePage(countryCode), BigDecimal(45000.00)).get
      val userAnswersWithCountry: UserAnswers =
        userAnswers.set(IncomeSourceCountries, Array(Country(countryName, countryCode))).get

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCountry), isAgent = false).build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.foreign.expenses.routes.ForeignPropertyExpensesStartController
            .onPageLoad(taxYear, countryCode)
            .url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignPropertyExpensesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          taxYear,
          countryName,
          isIncomeUnder85k = true,
          "individual",
          countryCode
        )(request, messages(application)).toString
      }
    }
    "must return OK and the correct view for a GET if income over 85k" in {
      val userAnswers: UserAnswers =
        UserAnswers("test").set(ForeignPropertyRentalIncomePage(countryCode), BigDecimal(85000.00)).get
      val userAnswersWithCountry: UserAnswers =
        userAnswers.set(IncomeSourceCountries, Array(Country(countryName, countryCode))).get

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCountry), isAgent = false).build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.foreign.expenses.routes.ForeignPropertyExpensesStartController
            .onPageLoad(taxYear, countryCode)
            .url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignPropertyExpensesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          taxYear,
          countryName,
          isIncomeUnder85k = false,
          "individual",
          countryCode
        )(request, messages(application)).toString
      }
    }
  }
}
