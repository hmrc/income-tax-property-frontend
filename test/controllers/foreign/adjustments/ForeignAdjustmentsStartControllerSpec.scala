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

package controllers.foreign.adjustments

import base.SpecBase
import models.{NormalMode, UserAnswers}
import pages.foreign.{Country, IncomeSourceCountries}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.foreign.adjustments.ForeignAdjustmentsStartView

class ForeignAdjustmentsStartControllerSpec extends SpecBase {

  val taxYear = 2023
  val countryCode = "AUS"
  val countryName = "Australia"
  val isPIA = true

  "ForeignAdjustmentsStart Controller" - {

    "must return OK and the correct view for a GET" in {
      val continueLink = controllers.foreign.adjustments.routes.ForeignPrivateUseAdjustmentController.onPageLoad(taxYear, countryCode, NormalMode).url
      val userAnswers = UserAnswers("test").set(IncomeSourceCountries, Array(Country(countryName, countryCode))).get

      val application = applicationBuilder(userAnswers = Some(userAnswers),isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, controllers.foreign.adjustments.routes.ForeignAdjustmentsStartController.onPageLoad(taxYear, countryCode, isPIA).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignAdjustmentsStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, countryName, countryCode, isPIA, isUkAndForeignJourney = false, continueLink)(request, messages(application)).toString
      }
    }
  }
}
