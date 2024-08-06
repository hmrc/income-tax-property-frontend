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
import forms.propertyrentals.expenses.RentsRatesAndInsuranceFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.propertyrentals.expenses.RentsRatesAndInsurancePage
import play.api.http.Status.OK
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status}
import repositories.SessionRepository
import views.html.propertyrentals.expenses.RentsRatesAndInsuranceView
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class RentsRatesAndInsuranceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/expenses/repairs-and-maintenance-costs")
  private val taxYear = LocalDate.now.getYear

  val formProvider = new RentsRatesAndInsuranceFormProvider()
  val form = formProvider("individual")

  lazy val rentalsRentsRoute = routes.RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode, Rentals).url
  lazy val rentalsAndRaRRentsRoute =
    routes.RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "RentsRatesAndInsurance Controller" - {

    "For rentals only must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRentsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RentsRatesAndInsuranceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual", Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "For rentals and RaR must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsAndRaRRentsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RentsRatesAndInsuranceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual", RentalsRentARoom)(
          request,
          messages(application)
        ).toString
      }
    }

    "For rentals only must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(RentsRatesAndInsurancePage(Rentals), BigDecimal(12.34)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRentsRoute)

        val view = application.injector.instanceOf[RentsRatesAndInsuranceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(BigDecimal(12.34)),
          taxYear,
          NormalMode,
          "individual",
          Rentals
        )(request, messages(application)).toString
      }
    }

    "For rentals and RaR must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(RentsRatesAndInsurancePage(RentalsRentARoom), BigDecimal(12.34)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsAndRaRRentsRoute)

        val view = application.injector.instanceOf[RentsRatesAndInsuranceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(BigDecimal(12.34)),
          taxYear,
          NormalMode,
          "individual",
          RentalsRentARoom
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
          FakeRequest(POST, rentalsRentsRoute)
            .withFormUrlEncodedBody("rentsRatesAndInsurance" -> "1234")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRentsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RentsRatesAndInsuranceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "individual", Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRentsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRentsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
