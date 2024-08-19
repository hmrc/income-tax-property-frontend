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

package controllers.allowances

import base.SpecBase
import controllers.allowances.routes._
import controllers.routes
import forms.allowances.OtherCapitalAllowanceFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.allowances.OtherCapitalAllowancePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.allowances.OtherCapitalAllowanceView

import scala.concurrent.Future

class OtherCapitalAllowanceControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new OtherCapitalAllowanceFormProvider()
  private val isAgentMessageKey = "individual"
  val form: Form[BigDecimal] = formProvider(isAgentMessageKey)

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: BigDecimal = BigDecimal(0)
  val taxYear = 2023

  lazy val rentalsOtherCapitalAllowanceRoute =
    OtherCapitalAllowanceController.onPageLoad(taxYear, NormalMode, Rentals).url

  lazy val rentalsRentARoomOtherCapitalAllowanceRoute =
    OtherCapitalAllowanceController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "OtherCapitalAllowance Controller" - {

    "must return OK and the correct view for a GET for both Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val view = application.injector.instanceOf[OtherCapitalAllowanceView]

        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsOtherCapitalAllowanceRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(
          form,
          taxYear,
          isAgentMessageKey,
          NormalMode,
          Rentals
        )(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals & Rent a Room
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomOtherCapitalAllowanceRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual OK
        contentAsString(rentalsRentARoomResult) mustEqual view(
          form,
          taxYear,
          isAgentMessageKey,
          NormalMode,
          RentalsRentARoom
        )(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for both Rentals and Rentals and Rent a Room journeys" in {

      val rentalsUserAnswers =
        UserAnswers(userAnswersId).set(OtherCapitalAllowancePage(Rentals), validAnswer).success.value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), isAgent = false).build()

      val view = rentalsApplication.injector.instanceOf[OtherCapitalAllowanceView]

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsOtherCapitalAllowanceRoute)
        val result = route(rentalsApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, isAgentMessageKey, NormalMode, Rentals)(
          request,
          messages(rentalsApplication)
        ).toString
      }

      val rentalsRentRoomUserAnswers =
        UserAnswers(userAnswersId).set(OtherCapitalAllowancePage(RentalsRentARoom), validAnswer).success.value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentRoomUserAnswers), isAgent = false).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomOtherCapitalAllowanceRoute)
        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(validAnswer),
          taxYear,
          isAgentMessageKey,
          NormalMode,
          RentalsRentARoom
        )(
          request,
          messages(rentalsRentARoomApplication)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted for both Rentals and Rentals and Rent a Room journeys" in {

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
        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsOtherCapitalAllowanceRoute)
            .withFormUrlEncodedBody(("otherCapitalAllowanceAmount", validAnswer.toString))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual onwardRoute.url

        // Rentals & Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomOtherCapitalAllowanceRoute)
            .withFormUrlEncodedBody(("otherCapitalAllowanceAmount", validAnswer.toString))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for both Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val boundForm = form.bind(Map("otherCapitalAllowanceAmount" -> "invalid value"))
      val view = application.injector.instanceOf[OtherCapitalAllowanceView]

      running(application) {

        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsOtherCapitalAllowanceRoute)
            .withFormUrlEncodedBody(("otherCapitalAllowanceAmount", "invalid value"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(boundForm, taxYear, isAgentMessageKey, NormalMode, Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals & Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomOtherCapitalAllowanceRoute)
            .withFormUrlEncodedBody(("otherCapitalAllowanceAmount", "invalid value"))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual BAD_REQUEST
        contentAsString(rentalsRentARoomResult) mustEqual view(
          boundForm,
          taxYear,
          isAgentMessageKey,
          NormalMode,
          RentalsRentARoom
        )(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found for both Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsOtherCapitalAllowanceRoute)

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        // Rentals & Rent a Room
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomOtherCapitalAllowanceRoute)

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found for both Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {

        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsOtherCapitalAllowanceRoute)
            .withFormUrlEncodedBody(("otherCapitalAllowanceAmount", validAnswer.toString))

        val rentalsResult = route(application, rentalsRequest).value
        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        // Rentals & Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomOtherCapitalAllowanceRoute)
            .withFormUrlEncodedBody(("otherCapitalAllowanceAmount", validAnswer.toString))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value
        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
