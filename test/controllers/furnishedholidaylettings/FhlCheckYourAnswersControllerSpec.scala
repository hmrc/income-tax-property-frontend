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

package controllers.furnishedholidaylettings

import base.SpecBase
import models.User
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.furnishedholidaylettings.FhlCheckYourAnswersView

class FhlCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  private val taxYear = 2024
  private val scenarios = Table[Boolean, String](
    ("isAgency", "AgencyOrIndividual"),
    (true, "agent"),
    (false, "individual"))

  forAll(scenarios) { (isAgency: Boolean, agencyOrIndividual: String) => {
    s"Check Your Answers Controller for $agencyOrIndividual" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgency).build()

        running(application) {
          val request = FakeRequest(GET, controllers.furnishedholidaylettings.routes.FhlCheckYourAnswersController.onPageLoad(taxYear).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[FhlCheckYourAnswersView]
          val list = SummaryListViewModel(Seq.empty)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(taxYear, list)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgency).build()

        running(application) {
          val request = FakeRequest(GET, controllers.furnishedholidaylettings.routes.FhlCheckYourAnswersController.onPageLoad(taxYear).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
  }
}
