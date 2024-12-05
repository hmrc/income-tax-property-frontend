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
import forms.adjustments.PrivateUseAdjustmentFormProvider
import models.{NormalMode, PrivateUseAdjustment, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustments.PrivateUseAdjustmentPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.adjustments.PrivateUseAdjustmentView

import java.time.LocalDate
import scala.concurrent.Future

class PrivateUseAdjustmentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/private-use-adjustment")

  val taxYear: Int = LocalDate.now.getYear
  lazy val rentalsPrivateUseAdjustmentRoute: String =
    routes.PrivateUseAdjustmentController.onPageLoad(taxYear, NormalMode, Rentals).url
  lazy val rentalsRentARoomPrivateUseAdjustmentRoute: String =
    routes.PrivateUseAdjustmentController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  val formProvider = new PrivateUseAdjustmentFormProvider()
  val form: Form[PrivateUseAdjustment] = formProvider("individual")

  "PrivateUseAdjustmentController Controller" - {

    "must return OK and the correct view for a GET when an individual for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      val view = application.injector.instanceOf[PrivateUseAdjustmentView]

      running(application) {
        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsPrivateUseAdjustmentRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, taxYear, NormalMode, "individual", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rentals and Rent a Room
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomPrivateUseAdjustmentRoute)
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

    "must return OK and the correct view for a GET when an agent for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val view = application.injector.instanceOf[PrivateUseAdjustmentView]

      running(application) {
        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsPrivateUseAdjustmentRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, taxYear, NormalMode, "agent", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rentals and Rent a Room
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomPrivateUseAdjustmentRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual OK
        contentAsString(rentalsRentARoomResult) mustEqual view(form, taxYear, NormalMode, "agent", RentalsRentARoom)(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for both the Rentals and Rentals and Rent a Room journeys" in {

      val rentalsUserAnswers =
        UserAnswers(userAnswersId).set(PrivateUseAdjustmentPage(Rentals), PrivateUseAdjustment(7689.23)).success.value
      val rentalsRentARoomUserAnswers =
        UserAnswers(userAnswersId)
          .set(PrivateUseAdjustmentPage(RentalsRentARoom), PrivateUseAdjustment(7689.23))
          .success
          .value

      // Rentals
      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), isAgent = true).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsPrivateUseAdjustmentRoute)
        val view = rentalsApplication.injector.instanceOf[PrivateUseAdjustmentView]
        val result = route(rentalsApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(PrivateUseAdjustment(7689.23)),
          taxYear,
          NormalMode,
          "agent",
          Rentals
        )(
          request,
          messages(rentalsApplication)
        ).toString
      }

      // Rentals
      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), isAgent = true).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomPrivateUseAdjustmentRoute)
        val view = rentalsRentARoomApplication.injector.instanceOf[PrivateUseAdjustmentView]
        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(PrivateUseAdjustment(7689.23)),
          taxYear,
          NormalMode,
          "agent",
          RentalsRentARoom
        )(
          request,
          messages(rentalsRentARoomApplication)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted for yes for both the Rentals and Rentals and Rent a Room journeys" in {

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
          FakeRequest(POST, rentalsPrivateUseAdjustmentRoute)
            .withFormUrlEncodedBody("privateUseAdjustmentAmount" -> "648.98")

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual onwardRoute.url

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomPrivateUseAdjustmentRoute)
            .withFormUrlEncodedBody("privateUseAdjustmentAmount" -> "648.98")

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val boundForm = form.bind(Map("privateUseAdjustmentAmount" -> "87.858585"))
        val view = application.injector.instanceOf[PrivateUseAdjustmentView]

        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsPrivateUseAdjustmentRoute)
            .withFormUrlEncodedBody(("privateUseAdjustmentAmount", "87.858585"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(boundForm, taxYear, NormalMode, "individual", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomPrivateUseAdjustmentRoute)
            .withFormUrlEncodedBody(("privateUseAdjustmentAmount", "87.858585"))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual BAD_REQUEST
        contentAsString(rentalsRentARoomResult) mustEqual view(
          boundForm,
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
  }
}
