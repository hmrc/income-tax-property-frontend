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

package controllers.propertyrentals.expenses
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import base.SpecBase
import controllers.propertyrentals.expenses.routes
import forms.ConsolidatedExpensesFormProvider
import models.{ConsolidatedExpenses, NormalMode, PropertyType, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.propertyrentals.expenses.ConsolidatedExpensesView

import java.time.LocalDate
import scala.concurrent.Future

class ConsolidatedExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/")
  private val taxYear = LocalDate.now.getYear

  val formProvider = new ConsolidatedExpensesFormProvider()
  val form = formProvider("individual")

  val scenarios = Table[PropertyType, String](
    ("property type", "type definition"),
    (RentalsRentARoom, "rentalsAndRaR"),
    (Rentals, "rentals")
  )

  forAll(scenarios) { (propertyType: PropertyType, propertyTypeDefinition: String) =>
    lazy val consolidatedExpensesRoute =
      routes.ConsolidatedExpensesController.onPageLoad(taxYear, NormalMode, propertyType).url

    s"ConsolidatedExpenses Controller for $propertyTypeDefinition" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

        running(application) {
          val request = FakeRequest(GET, consolidatedExpensesRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ConsolidatedExpensesView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, taxYear, "individual", propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(ConsolidatedExpensesPage(propertyType), ConsolidatedExpenses(true, Some(12.34)))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

        running(application) {
          val request = FakeRequest(GET, consolidatedExpensesRoute)

          val view = application.injector.instanceOf[ConsolidatedExpensesView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(ConsolidatedExpenses(true, Some(12.34))),
            NormalMode,
            taxYear,
            "individual",
            propertyType
          )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), false)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, consolidatedExpensesRoute)
              .withFormUrlEncodedBody("consolidatedExpensesYesOrNo" -> "true", "consolidatedExpensesAmount" -> "1234")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

        running(application) {
          val request =
            FakeRequest(POST, consolidatedExpensesRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[ConsolidatedExpensesView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, "individual", propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val request = FakeRequest(GET, consolidatedExpensesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val request =
            FakeRequest(POST, consolidatedExpensesRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
