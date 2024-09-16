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

package controllers

import base.SpecBase
import models.Rentals
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.summarylist._
import views.html.structurebuildingallowance.ClaimSbaCheckYourAnswersView
class ClaimSbaCheckYourAnswersControllerSpec extends SpecBase {

  val taxYear = 2024

  "ClaimSbaCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val list = SummaryListViewModel(Seq.empty)
      running(application) {
        val request = FakeRequest(
          GET,
          controllers.structuresbuildingallowance.routes.ClaimSbaCheckYourAnswersController
            .onPageLoad(taxYear, Rentals)
            .url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClaimSbaCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear, Rentals)(request, messages(application)).toString
      }
    }
  }
}
