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

package controllers.rentalsandrentaroom.expenses

import base.SpecBase
import models.{ConsolidatedExpenses, JourneyContext, RentalsAndRentARoomExpenses, RentalsRentARoom, User, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.rentalsandrentaroom.expenses.RentalsAndRaRExpensesCheckYourAnswersView
import org.mockito.Mockito.when
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.inject.bind

class RentalsAndRaRExpensesCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  private val taxYear = 2024
  private val propertySubmissionService = mock[PropertySubmissionService]

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
    s"RentalsAndRentARoomExpenseCheckYourAnswers Controller for $agencyOrIndividual" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController
              .onPageLoad(taxYear)
              .url
          )

          val result = route(application, request).value

          val list = SummaryListViewModel(Seq.empty)

          val view = application.injector.instanceOf[RentalsAndRaRExpensesCheckYourAnswersView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
        }
      }
    }

    s"must return OK and the correct view for a POST (onSubmit) for $agencyOrIndividual" in {

      val consolidatedExpenses = ConsolidatedExpenses(consolidatedExpensesYesOrNo = true, Some(12))
      val userAnswers =
        UserAnswers("test").set(ConsolidatedExpensesPage(RentalsRentARoom), consolidatedExpenses).toOption

      val context =
        JourneyContext(
          taxYear = taxYear,
          mtditid = "mtditid",
          nino = "nino",
          journeyName = "rentals-and-rent-a-room-expenses"
        )
      val rentalsExpense =
        RentalsAndRentARoomExpenses(Some(consolidatedExpenses), None, None, None, None, None, None, None)

      when(
        propertySubmissionService
          .saveJourneyAnswers(ArgumentMatchers.eq(context), ArgumentMatchers.eq(rentalsExpense))(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.RentalsAndRaRExpensesCheckYourAnswersController.onSubmit(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        // redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
