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

package controllers.foreigndividends

import base.SpecBase
import controllers.routes
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.foreigndividends.ForeignDividendsStartView

class ForeignDividendsStartControllerSpec extends SpecBase {

  "ForeignDividendsStart Controller" - {

    "must return OK and the correct view for a GET" in {
      val taxYear: Int = 2024
      val isAgentMessageKey: String = "individual"
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, controllers.foreigndividends.routes.ForeignDividendsStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignDividendsStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, isAgentMessageKey)(request, messages(application)).toString
      }
    }
  }
}
