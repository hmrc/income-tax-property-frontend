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

package controllers.propertyrentals

import base.SpecBase
import controllers.{propertyrentals, routes}
import forms.propertyrentals.ClaimPropertyIncomeAllowanceFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.propertyrentals.ClaimPropertyIncomeAllowanceView

import java.time.LocalDate
import scala.concurrent.Future

class ClaimPropertyIncomeAllowanceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ClaimPropertyIncomeAllowanceFormProvider()
  val form = formProvider("individual")
  val taxYear = LocalDate.now.getYear

  lazy val rentalsClaimPropertyIncomeAllowanceRoute =
    propertyrentals.routes.ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode, Rentals).url

  lazy val rentalsAndRentARoomClaimPropertyIncomeAllowanceRoute =
    propertyrentals.routes.ClaimPropertyIncomeAllowanceController
      .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
      .url

  "ClaimPropertyIncomeAllowance Controller" - {

    "must return OK and the correct view for a GET for both rentals and rentals and rent a room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val rentalsRequest = FakeRequest(GET, rentalsClaimPropertyIncomeAllowanceRoute)
        val rentalsResult = route(application, rentalsRequest).value
        val view = application.injector.instanceOf[ClaimPropertyIncomeAllowanceView]

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, taxYear, NormalMode, Rentals, "individual")(
          rentalsRequest,
          messages(application)
        ).toString

        val rentalsAndRentARoomRequest = FakeRequest(GET, rentalsAndRentARoomClaimPropertyIncomeAllowanceRoute)
        val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value

        status(rentalsAndRentARoomResult) mustEqual OK
        contentAsString(rentalsAndRentARoomResult) mustEqual view(
          form,
          taxYear,
          NormalMode,
          RentalsRentARoom,
          "individual"
        )(
          rentalsAndRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for both rentals and rentals and rent a room journeys" in {

      val rentalsUserAnswers =
        UserAnswers(userAnswersId).set(ClaimPropertyIncomeAllowancePage(Rentals), true).success.value
      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), isAgent = false).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsClaimPropertyIncomeAllowanceRoute)
        val view = rentalsApplication.injector.instanceOf[ClaimPropertyIncomeAllowanceView]
        val result = route(rentalsApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), taxYear, NormalMode, Rentals, "individual")(
          request,
          messages(rentalsApplication)
        ).toString
      }

      val rentalsAndRentARoomUserAnswers =
        UserAnswers(userAnswersId).set(ClaimPropertyIncomeAllowancePage(RentalsRentARoom), true).success.value

      val rentalsAndRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsAndRentARoomUserAnswers), isAgent = false).build()

      running(rentalsAndRentARoomApplication) {
        val request = FakeRequest(GET, rentalsAndRentARoomClaimPropertyIncomeAllowanceRoute)
        val view = rentalsAndRentARoomApplication.injector.instanceOf[ClaimPropertyIncomeAllowanceView]
        val result = route(rentalsAndRentARoomApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), taxYear, NormalMode, RentalsRentARoom, "individual")(
          request,
          messages(rentalsApplication)
        ).toString
      }

    }

    "must redirect to the next page when valid data is submitted for both rentals and rentals and rent a room journeys" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val rentalsRequest =
          FakeRequest(POST, rentalsClaimPropertyIncomeAllowanceRoute)
            .withFormUrlEncodedBody(("claimPropertyIncomeAllowanceYesOrNo", "true"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual onwardRoute.url

        val rentalsAndRentARoomRequest =
          FakeRequest(POST, rentalsAndRentARoomClaimPropertyIncomeAllowanceRoute)
            .withFormUrlEncodedBody(("claimPropertyIncomeAllowanceYesOrNo", "true"))

        val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value

        status(rentalsAndRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsAndRentARoomResult).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for both rentals and rentals and rent a room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val rentalsRequest =
          FakeRequest(POST, rentalsClaimPropertyIncomeAllowanceRoute)
            .withFormUrlEncodedBody(("claimPropertyIncomeAllowanceYesOrNo", ""))

        val boundForm = form.bind(Map("claimPropertyIncomeAllowanceYesOrNo" -> ""))
        val view = application.injector.instanceOf[ClaimPropertyIncomeAllowanceView]
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(boundForm, taxYear, NormalMode, Rentals, "individual")(
          rentalsRequest,
          messages(application)
        ).toString

        val rentalsAndRentARoomRequest =
          FakeRequest(POST, rentalsAndRentARoomClaimPropertyIncomeAllowanceRoute)
            .withFormUrlEncodedBody(("claimPropertyIncomeAllowanceYesOrNo", ""))

        val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value

        status(rentalsAndRentARoomResult) mustEqual BAD_REQUEST
        contentAsString(rentalsAndRentARoomResult) mustEqual view(
          boundForm,
          taxYear,
          NormalMode,
          RentalsRentARoom,
          "individual"
        )(
          rentalsAndRentARoomRequest,
          messages(application)
        ).toString

      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found for both rentals and rentals and rent a room journeys" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val rentalsRequest = FakeRequest(GET, rentalsClaimPropertyIncomeAllowanceRoute)
        val rentalsResult = route(application, rentalsRequest).value
        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        val rentalsAndRentARoomRequest = FakeRequest(GET, rentalsAndRentARoomClaimPropertyIncomeAllowanceRoute)
        val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value
        status(rentalsAndRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsAndRentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found for both rentals and rentals and rent a room journeys" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val rentalsRequest =
          FakeRequest(POST, rentalsClaimPropertyIncomeAllowanceRoute)
            .withFormUrlEncodedBody(("claimPropertyIncomeAllowanceYesOrNo", "true"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        val rentalsAndRentARoomRequest =
          FakeRequest(POST, rentalsAndRentARoomClaimPropertyIncomeAllowanceRoute)
            .withFormUrlEncodedBody(("claimPropertyIncomeAllowanceYesOrNo", "true"))

        val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value

        status(rentalsAndRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsAndRentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
