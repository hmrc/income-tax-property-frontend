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

package controllers.premiumlease

import base.SpecBase
import forms.premiumlease.CalculatedFigureYourselfFormProvider
import models.{CalculatedFigureYourself, NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.premiumlease.CalculatedFigureYourselfPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.premiumlease.CalculatedFigureYourselfView

import java.time.LocalDate
import scala.concurrent.Future

class CalculatedFigureYourselfControllerSpec extends SpecBase with MockitoSugar {

  def onwardRouteNo = Call("GET", "/received-grant-lease-amount")
  def onwardRouteYes = Call("GET", "/reverse-premiums-received")

  private val formProvider = new CalculatedFigureYourselfFormProvider()
  private val form = formProvider("individual")
  private val taxYear = LocalDate.now.getYear

  private lazy val rentalsCalculatedFigureYourselfRoute =
    routes.CalculatedFigureYourselfController.onPageLoad(taxYear, NormalMode, Rentals).url

  private lazy val rentalsRentARoomCalculatedFigureYourselfRoute =
    routes.CalculatedFigureYourselfController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "CalculatedFigureYourself Controller" - {

    "must return OK and the correct view for a GET for Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {

        val view = application.injector.instanceOf[CalculatedFigureYourselfView]

        val rentalsRequest = FakeRequest(GET, rentalsCalculatedFigureYourselfRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, taxYear, NormalMode, "individual", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomCalculatedFigureYourselfRoute)
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

    "must populate the view correctly on a GET when the question has previously been answered for Rentals and Rentals and Rent a Room journeys" in {

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(CalculatedFigureYourselfPage(Rentals), CalculatedFigureYourself(true, Some(3242.65)))
        .success
        .value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), isAgent = false).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsCalculatedFigureYourselfRoute)
        val view = rentalsApplication.injector.instanceOf[CalculatedFigureYourselfView]
        val result = route(rentalsApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(CalculatedFigureYourself(true, Some(3242.65))),
          taxYear,
          NormalMode,
          "individual",
          Rentals
        )(request, messages(rentalsApplication)).toString
      }

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(CalculatedFigureYourselfPage(RentalsRentARoom), CalculatedFigureYourself(true, Some(3242.65)))
        .success
        .value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), isAgent = false).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomCalculatedFigureYourselfRoute)
        val view = rentalsRentARoomApplication.injector.instanceOf[CalculatedFigureYourselfView]
        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(CalculatedFigureYourself(true, Some(3242.65))),
          taxYear,
          NormalMode,
          "individual",
          RentalsRentARoom
        )(request, messages(rentalsRentARoomApplication)).toString
      }

    }

    "must redirect to the next page when valid data is submitted for yes for Rentals and Rentals and Rent a Room journeys" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

      val rentalsUserData =
        emptyUserAnswers.set(CalculatedFigureYourselfPage(Rentals), CalculatedFigureYourself(true, Some(866.65))).get

      // Rentals
      val rentalsApplication =
        applicationBuilder(userAnswers = Some(rentalsUserData), isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteYes)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(rentalsApplication) {
        val request =
          FakeRequest(POST, rentalsCalculatedFigureYourselfRoute)
            .withFormUrlEncodedBody("calculatedFigureYourself" -> "true", "calculatedFigureYourselfAmount" -> "866.65")

        val result = route(rentalsApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteYes.url
      }

      // Rent a room journey
      val rentalsRentARoomUserData =
        emptyUserAnswers
          .set(CalculatedFigureYourselfPage(RentalsRentARoom), CalculatedFigureYourself(true, Some(866.65)))
          .get

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserData), isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteYes)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(rentalsRentARoomApplication) {
        val request =
          FakeRequest(POST, rentalsRentARoomCalculatedFigureYourselfRoute)
            .withFormUrlEncodedBody("calculatedFigureYourself" -> "true", "calculatedFigureYourselfAmount" -> "866.65")

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteYes.url
      }
    }

    "must redirect to the next page when valid data is submitted for no" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

      val rentalsUserData =
        emptyUserAnswers.set(CalculatedFigureYourselfPage(Rentals), CalculatedFigureYourself(false, None)).get

      val rentalsApplication =
        applicationBuilder(userAnswers = Some(rentalsUserData), isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteNo)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(rentalsApplication) {
        val request =
          FakeRequest(POST, rentalsCalculatedFigureYourselfRoute)
            .withFormUrlEncodedBody("calculatedFigureYourself" -> "false")

        val result = route(rentalsApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteNo.url
      }

      // Rent a room journey
      val rentalsRentARoomUserData =
        emptyUserAnswers.set(CalculatedFigureYourselfPage(RentalsRentARoom), CalculatedFigureYourself(false, None)).get

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserData), isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteNo)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(rentalsRentARoomApplication) {
        val request =
          FakeRequest(POST, rentalsRentARoomCalculatedFigureYourselfRoute)
            .withFormUrlEncodedBody("calculatedFigureYourself" -> "false")

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteNo.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {

        // Rentals journey
        val rentalsRequest =
          FakeRequest(POST, rentalsCalculatedFigureYourselfRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[CalculatedFigureYourselfView]
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(boundForm, taxYear, NormalMode, "individual", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rent a room journey
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomCalculatedFigureYourselfRoute)
            .withFormUrlEncodedBody(("value", ""))

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

    "must redirect to Journey Recovery for a GET if no existing data is found for Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {

        // Rentals journey
        val rentalsRequest = FakeRequest(GET, rentalsCalculatedFigureYourselfRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        // Rentals and rent a room journey
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomCalculatedFigureYourselfRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url

      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found for Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {

        // Rentals journey
        val rentalsRequest =
          FakeRequest(POST, rentalsCalculatedFigureYourselfRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        // Rentals and Rent a Room journey
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomCalculatedFigureYourselfRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }
  }
}
