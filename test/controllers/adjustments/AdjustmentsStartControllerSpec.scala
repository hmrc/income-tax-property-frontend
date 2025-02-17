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

package controllers.adjustments

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.adjustments.AdjustmentsStartView

class AdjustmentsStartControllerSpec extends SpecBase {

  val taxYear = 2023

  "AdjustmentsStart Controller" - {

    "must return OK and the correct view for an individual" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers),isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, true).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentsStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, true, "individual")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers),isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, controllers.adjustments.routes.AdjustmentsStartController.onPageLoad(taxYear, true).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentsStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, true, "agent")(request, messages(application)).toString
      }
    }
  }
}
