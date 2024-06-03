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

package controllers.ukrentaroom.expenses

import base.SpecBase
import controllers.ukrentaroom.expenses.routes._
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.ukrentaroom.expenses.ExpensesCheckYourAnswersRRView

class ExpensesCheckYourAnswersRRControllerSpec extends SpecBase with SummaryListFluency {

  def onwardRoute: Call = Call("GET", "/update-and-submit-income-tax-return/property/2023/summary")

  "ExpensesCheckYourRRAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val list = SummaryListViewModel(Seq.empty)
      val taxYear = 2023
      running(application) {

        val request = FakeRequest(GET, ExpensesCheckYourAnswersRRController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExpensesCheckYourAnswersRRView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val taxYear = 2023
      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, ExpensesCheckYourAnswersRRController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a POST (onSubmit)" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val taxYear = 2023

      running(application) {
        val request = FakeRequest(POST, routes.ExpensesCheckYourAnswersRRController.onSubmit(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
