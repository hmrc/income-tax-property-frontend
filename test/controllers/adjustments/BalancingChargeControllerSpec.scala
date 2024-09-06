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

package controllers.adjustments

import base.SpecBase
import forms.adjustments.BalancingChargeFormProvider
import models.Rentals
import models.{BalancingCharge, NormalMode, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustments.BalancingChargePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.adjustments.BalancingChargeView

import java.time.LocalDate
import scala.concurrent.Future

class BalancingChargeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/balanceCharge")

  val taxYear: Int = LocalDate.now.getYear
  lazy val rentalsBalancingChargeRoute: String =
    routes.BalancingChargeController.onPageLoad(taxYear, NormalMode, Rentals).url
  lazy val rentalsRentARoomBalancingChargeRoute: String =
    routes.BalancingChargeController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  val formProvider = new BalancingChargeFormProvider()
  val form: Form[BalancingCharge] = formProvider("agent")

  "BalancingCharge Controller" - {

    "must return OK and the correct view for a GET when an individual for both Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val view = application.injector.instanceOf[BalancingChargeView]

      running(application) {
        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsBalancingChargeRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, taxYear, NormalMode, "individual", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rent a Room
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomBalancingChargeRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual OK
        contentAsString(rentalsRentARoomResult) mustEqual view(
          form,
          taxYear,
          NormalMode,
          "individual",
          RentalsRentARoom
        )(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when an agent for both Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val view = application.injector.instanceOf[BalancingChargeView]

      running(application) {

        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsBalancingChargeRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, taxYear, NormalMode, "agent", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rent a Room
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomBalancingChargeRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual OK
        contentAsString(rentalsRentARoomResult) mustEqual view(form, taxYear, NormalMode, "agent", RentalsRentARoom)(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for both Rentals and Rentals and Rent a Room journeys" in {

      // Rentals
      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(BalancingChargePage(Rentals), BalancingCharge(balancingChargeYesNo = true, Some(7689.23)))
        .success
        .value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), isAgent = true).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsBalancingChargeRoute)
        val view = rentalsApplication.injector.instanceOf[BalancingChargeView]
        val result = route(rentalsApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(BalancingCharge(balancingChargeYesNo = true, Some(7689.23))),
          taxYear,
          NormalMode,
          "agent",
          Rentals
        )(request, messages(rentalsApplication)).toString
      }

      // Rentals and Rent a Room
      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(BalancingChargePage(RentalsRentARoom), BalancingCharge(balancingChargeYesNo = true, Some(7689.23)))
        .success
        .value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), isAgent = true).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomBalancingChargeRoute)
        val view = rentalsRentARoomApplication.injector.instanceOf[BalancingChargeView]
        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(BalancingCharge(balancingChargeYesNo = true, Some(7689.23))),
          taxYear,
          NormalMode,
          "agent",
          RentalsRentARoom
        )(request, messages(rentalsApplication)).toString
      }
    }

    "must redirect to the next page when valid data is submitted for yes for both Rentals and Rentals and Rent a Room journeys" in {

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
        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsBalancingChargeRoute)
            .withFormUrlEncodedBody("balancingChargeYesNo" -> "false")

        val result = route(application, rentalsRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomBalancingChargeRoute)
            .withFormUrlEncodedBody("balancingChargeYesNo" -> "false")

        val rentalsResult = route(application, rentalsRentARoomRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for both Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val boundForm = form.bind(Map("balancingChargeAmount" -> "87.858585"))
      val view = application.injector.instanceOf[BalancingChargeView]

      running(application) {

        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsBalancingChargeRoute)
            .withFormUrlEncodedBody(("balancingChargeAmount", "87.858585"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(boundForm, taxYear, NormalMode, "agent", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomBalancingChargeRoute)
            .withFormUrlEncodedBody(("balancingChargeAmount", "87.858585"))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual BAD_REQUEST
        contentAsString(rentalsRentARoomResult) mustEqual view(
          boundForm,
          taxYear,
          NormalMode,
          "agent",
          RentalsRentARoom
        )(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }
  }
}
