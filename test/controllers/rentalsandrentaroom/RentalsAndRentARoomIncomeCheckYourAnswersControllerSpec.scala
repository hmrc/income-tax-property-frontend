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
import models.User
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.rentalsandrentaroom.income.RentalsAndRentARoomIncomeCheckYourAnswersView

class RentalsAndRentARoomIncomeCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  private val taxYear = 2024

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
      isAgent,
      agentRef = Some("agentReferenceNumber")
    )
    s"RentalsAndRentARoomIncomeCheckYourAnswers Controller for $agencyOrIndividual" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
              .onPageLoad(taxYear)
              .url
          )

          val result = route(application, request).value

          val list = SummaryListViewModel(Seq.empty)

          val view = application.injector.instanceOf[RentalsAndRentARoomIncomeCheckYourAnswersView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
        }
      }
    }
  }
}
