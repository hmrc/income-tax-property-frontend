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

package controllers.structurebuildingallowance

import base.SpecBase
import controllers.structuresbuildingallowance.routes
import models.Rentals
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.summarylist._
import views.html.structurebuildingallowance.SbaCheckYourAnswersView

class SbaCheckYourAnswersControllerSpec extends SpecBase {
  val index = 1
  val taxYear = 2024

  "SbaCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val list = SummaryListViewModel(Seq.empty)
      running(application) {
        val request = FakeRequest(GET, routes.SbaCheckYourAnswersController.onPageLoad(taxYear, index, Rentals).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SbaCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear, Rentals)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.SbaCheckYourAnswersController.onPageLoad(taxYear, index, Rentals).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
