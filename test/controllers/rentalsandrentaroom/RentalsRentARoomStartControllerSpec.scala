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

package controllers.rentalsandrentaroom

import base.SpecBase
import models.requests.DataRequest
import models.{NormalMode, RentalsRentARoom, User}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rentalsandrentaroom.RentalsRentARoomStartView
class RentalsRentARoomStartControllerSpec extends SpecBase {
  private val taxYear = 2024
  "RentalsAndRentARoomStart Controller" - {
    val scenarios = Table[Boolean, String](
      ("isAgent", "individualOrAgent"),
      (false, "individual"),
      (true, "agent")
    )
    forAll(scenarios) { (isAgent: Boolean, agencyOrIndividual: String) =>
      val user = User(
        "",
        "",
        "",
        agentRef = Option.when(isAgent)("agentReferenceNumber")
      )
      s"must return OK and the correct view for a GET for $agencyOrIndividual" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.rentalsandrentaroom.routes.RentalsRentARoomStartController.onPageLoad(taxYear).url
          )

          val result = route(application, request).value
          val dataRequest =
            DataRequest(request, "", user, emptyUserAnswers)

          val view = application.injector.instanceOf[RentalsRentARoomStartView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(RentalsRentARoom, taxYear, NormalMode)(
            dataRequest,
            messages(application)
          ).toString
        }
      }
    }
  }
}
