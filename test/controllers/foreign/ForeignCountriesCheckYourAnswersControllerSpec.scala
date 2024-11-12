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

package controllers.foreign

import base.SpecBase
import controllers.foreign.routes.ForeignCountriesCheckYourAnswersController
import models.{ForeignTotalIncome, UserAnswers}
import pages.foreign.{ClaimPropertyIncomeAllowanceOrExpensesPage, Country, IncomeSourceCountries}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.ForeignCountriesCheckYourAnswersView

class ForeignCountriesCheckYourAnswersControllerSpec extends SpecBase {

  "ForeignPropertiesCheckYourAnswers Controller" - {

    val taxYear = 2024

    def submitOnwardRoute: Call = Call(
      "POST",
      "/update-and-submit-income-tax-return/property/2024/foreign-property/select-country/complete-yes-no"
    )

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val list = SummaryListViewModel(Seq.empty)

      running(application) {
        val request = FakeRequest(GET, ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignCountriesCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the Have you finished this section page" in {

      val userAnswers = UserAnswers("foreign-property-select-country")
        .set(pages.foreign.TotalIncomePage, ForeignTotalIncome.LessThanOneThousand)
        .flatMap { ua: UserAnswers =>
          ua.set(IncomeSourceCountries, Array(Country("USA", "US")))
        }
        .flatMap { ua =>
          ua.set(ClaimPropertyIncomeAllowanceOrExpensesPage, true)
        }
        .toOption

      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.ForeignCountriesCheckYourAnswersController.onSubmit(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual submitOnwardRoute.url
      }
    }
  }
}
