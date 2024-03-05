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

package controllers.propertyrentals.expenses

import base.SpecBase
import models.UserAnswers
import pages.propertyrentals.income.IncomeFromPropertyRentalsPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.propertyrentals.expenses.ExpensesStartView

class ExpensesStartControllerSpec extends SpecBase {

  "ExpensesStartOver85KIncome Controller" - {

    "must return OK and the correct view for a GET if Total Income below is 85K" in {
      val taxYear = 2023
      val userAnswers = UserAnswers("test").set(IncomeFromPropertyRentalsPage, BigDecimal(80000)).get
      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExpensesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, "agent", isUnder85K = true,
          "/update-and-submit-income-tax-return/property/2023/consolidated-expenses")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if Total Income is over 85K" in {
      val taxYear = 2023
      val userAnswers = UserAnswers("test").set(IncomeFromPropertyRentalsPage, BigDecimal(90000)).get

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, controllers.propertyrentals.expenses.routes.ExpensesStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExpensesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, "agent", isUnder85K = false,
          "/update-and-submit-income-tax-return/property/2023/expenses/rents-rates-and-insurance")(request, messages(application)).toString
      }
    }
  }
}
