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

package controllers.propertyrentals

import base.SpecBase
import controllers.propertyrentals
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.propertyrentals.PropertyRentalsStartView

import java.time.LocalDate

class PropertyRentalsStartControllerSpec extends SpecBase {

  val taxYear = LocalDate.now.getYear

  "PropertyRentalsStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyRentalsStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, "individual")(request, messages(application)).toString
      }
    }
  }
}
