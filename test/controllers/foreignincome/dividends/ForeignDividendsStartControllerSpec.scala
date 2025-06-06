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

package controllers.foreignincome.dividends

import controllers.foreignincome.dividends.routes.CountryReceiveDividendIncomeController
import base.SpecBase
import models.NormalMode
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.foreignincome.dividends.ForeignDividendsStartView

class ForeignDividendsStartControllerSpec extends SpecBase {

  val index = 0
  val taxYear = 2024
  val continueUrl: String = CountryReceiveDividendIncomeController.onPageLoad(taxYear, index, NormalMode).url

  "ForeignDividendsStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val isAgentMessageString: String = "individual"
      running(application) {
        val request = FakeRequest(GET, controllers.foreignincome.dividends.routes.ForeignDividendsStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignDividendsStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, continueUrl, isAgentMessageString)(request, messages(application)).toString
      }
    }
  }
}
