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

package controllers.rentalsandrentaroom.income

import base.SpecBase
import controllers.propertyrentals.income.routes.IsNonUKLandlordController
import models.{NormalMode, RentalsRentARoom}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rentalsandrentaroom.income.RentalsAndRentARoomIncomeStartView

class RentalsAndRentARoomIncomeStartControllerSpec extends SpecBase {
  val taxYear = 2024


  "RentalsAndRentARoomIncomeStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.RentalsAndRentARoomIncomeStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RentalsAndRentARoomIncomeStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, "agent")(request, messages(application)).toString
      }
    }
  }
}
