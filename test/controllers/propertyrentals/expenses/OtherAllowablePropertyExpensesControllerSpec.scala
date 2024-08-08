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

import base.SpecBase
import forms.OtherAllowablePropertyExpensesFormProvider
import models.{NormalMode, PropertyType, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.propertyrentals.expenses.OtherAllowablePropertyExpensesPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.propertyrentals.expenses.OtherAllowablePropertyExpensesView

import scala.concurrent.Future

class OtherAllowablePropertyExpensesControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new OtherAllowablePropertyExpensesFormProvider()
  private val agent = "agent"
  val form: Form[BigDecimal] = formProvider(agent)
  val taxYear = 2023

  def onwardRoute: Call = Call("GET", "/foo")

  val validAnswer: BigDecimal = BigDecimal(1000)
  val scenarios = Table[PropertyType, String](
    ("property type", "type definition"),
    (RentalsRentARoom, "rentalsAndRaR"),
    (Rentals, "rentals")
  )

  forAll(scenarios) { (propertyType: PropertyType, propertyTypeDefinition: String) =>
    lazy val OtherAllowablePropertyExpensesRoute: String =
      routes.OtherAllowablePropertyExpensesController.onPageLoad(taxYear, NormalMode, propertyType).url

    s"OtherAllowablePropertyExpenses Controller for $propertyTypeDefinition" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

        running(application) {
          val request = FakeRequest(GET, OtherAllowablePropertyExpensesRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OtherAllowablePropertyExpensesView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, agent, NormalMode, propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          UserAnswers(userAnswersId).set(OtherAllowablePropertyExpensesPage(propertyType), validAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

        running(application) {
          val request = FakeRequest(GET, OtherAllowablePropertyExpensesRoute)

          val view = application.injector.instanceOf[OtherAllowablePropertyExpensesView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, agent, NormalMode, propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, OtherAllowablePropertyExpensesRoute)
              .withFormUrlEncodedBody(("otherAllowablePropertyExpenses", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

        running(application) {
          val request =
            FakeRequest(POST, OtherAllowablePropertyExpensesRoute)
              .withFormUrlEncodedBody(("otherAllowablePropertyExpenses", "invalid non currency value"))

          val boundForm = form.bind(Map("otherAllowablePropertyExpenses" -> "invalid non currency value"))

          val view = application.injector.instanceOf[OtherAllowablePropertyExpensesView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, agent, NormalMode, propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).build()

        running(application) {
          val request = FakeRequest(GET, OtherAllowablePropertyExpensesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).build()

        running(application) {
          val request =
            FakeRequest(POST, OtherAllowablePropertyExpensesRoute)
              .withFormUrlEncodedBody(("otherAllowablePropertyExpenses", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
