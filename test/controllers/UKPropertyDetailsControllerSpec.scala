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

package controllers

import base.SpecBase
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.FakeAuthConnector
import testHelpers.Retrievals.Ops
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel}
import views.html.UKPropertyDetailsView


class UKPropertyDetailsControllerSpec extends SpecBase {

  "UKPropertyDetails Controller" - {

    "must return OK and the correct view for an Individual" in {
      val authConnector = new FakeAuthConnector(Some(Individual) ~ ConfidenceLevel.L200)

      val application =
        applicationBuilder(None,false)
          .overrides(bind[AuthConnector].toInstance(authConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.UKPropertyDetailsController.onPageLoad.url)
        val result = route(application, request).value

        val view = application.injector.instanceOf[UKPropertyDetailsView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view("individual")(request, messages(application)).toString

      }
    }

    "must return OK and the correct view for an Agent" in {
       val authConnector = new FakeAuthConnector(Some(Agent) ~ ConfidenceLevel.L200)

        val application =
          applicationBuilder(None,true)
            .overrides(bind[AuthConnector].toInstance(authConnector))
            .build()

      running(application) {
        val request = FakeRequest(GET, routes.UKPropertyDetailsController.onPageLoad.url)
        val result = route(application, request).value

        val view = application.injector.instanceOf[UKPropertyDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("agent")(request, messages(application)).toString
      }
     }
  }
}
