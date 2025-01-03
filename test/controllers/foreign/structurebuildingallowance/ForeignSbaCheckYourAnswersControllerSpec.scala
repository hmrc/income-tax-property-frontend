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

package controllers.foreign.structurebuildingallowance

import base.SpecBase
import controllers.routes
import controllers.foreign.structuresbuildingallowance.routes.ForeignSbaCheckYourAnswersController
import views.html.foreign.structurebuildingallowance.ForeignSbaCheckYourAnswersView
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.all.SummaryListViewModel

class ForeignSbaCheckYourAnswersControllerSpec extends SpecBase {
  val index: Int = 1
  val countryCode:String = "AUS"
  val taxYear: Int = 2024
  "ForeignSbaCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val list = SummaryListViewModel(Seq.empty)

      running(application) {
        val request = FakeRequest(GET, ForeignSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode, index).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignSbaCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear, countryCode)(request, messages(application)).toString
      }
    }
    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, ForeignSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode, index).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
