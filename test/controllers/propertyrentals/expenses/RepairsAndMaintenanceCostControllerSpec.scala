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
import forms.propertyrentals.expenses.RepairsAndMaintenanceCostsFormProvider
import models.{NormalMode, PropertyType, Rentals, RentalsRentARoom, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status}
import repositories.SessionRepository
import views.html.propertyrentals.expenses.ReparisAndMaintenanceCostsView
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.propertyrentals.expenses.RepairsAndMaintenanceCostsPage
import play.api.inject.bind
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._

class RepairsAndMaintenanceCostControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/")
  private val taxYear = LocalDate.now.getYear

  val formProvider = new RepairsAndMaintenanceCostsFormProvider()
  val form = formProvider("individual")
  val scenarios = Table[PropertyType, String](
    ("property type", "type definition"),
    (RentalsRentARoom, "rentalsAndRaR"),
    (Rentals, "rentals")
  )

  forAll(scenarios) { (propertyType: PropertyType, propertyTypeDefinition: String) =>
    lazy val rentsRoute = routes.RepairsAndMaintenanceCostsController.onPageLoad(taxYear, NormalMode, propertyType).url

    s"RentsRatesAndInsurance Controller for $propertyTypeDefinition" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

        running(application) {
          val request = FakeRequest(GET, rentsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ReparisAndMaintenanceCostsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual", propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          UserAnswers(userAnswersId).set(RepairsAndMaintenanceCostsPage(propertyType), BigDecimal(12.34)).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

        running(application) {
          val request = FakeRequest(GET, rentsRoute)

          val view = application.injector.instanceOf[ReparisAndMaintenanceCostsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(BigDecimal(12.34)),
            taxYear,
            NormalMode,
            "individual",
            propertyType
          )(
            request,
            messages(application)
          ).toString
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
            FakeRequest(POST, rentsRoute)
              .withFormUrlEncodedBody("repairsAndMaintenanceCosts" -> "1234")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

        running(application) {
          val request =
            FakeRequest(POST, rentsRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[ReparisAndMaintenanceCostsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "individual", propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val request = FakeRequest(GET, rentsRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val request =
            FakeRequest(POST, rentsRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
