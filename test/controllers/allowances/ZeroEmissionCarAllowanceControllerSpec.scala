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
import forms.allowances.ZeroEmissionCarAllowanceFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.allowances.ZeroEmissionCarAllowancePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.allowances.ZeroEmissionCarAllowanceView

import scala.concurrent.Future

class ZeroEmissionCarAllowanceControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new ZeroEmissionCarAllowanceFormProvider()
  private val individual = "individual"
  val form: Form[BigDecimal] = formProvider(individual)
  val taxYear = 2023

  def onwardRoute: Call = Call("GET", "/foo")

  val validAnswer: BigDecimal = 100

  lazy val rentalsZeroEmissionCarAllowanceRoute: String =
    ZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode, Rentals).url

  lazy val rentalsRentARoomZeroEmissionCarAllowanceRoute: String =
    ZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "ZeroEmissionCarAllowance Controller" - {

    "must return OK and the correct view for a GET both for the rentals and the combined journey" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val view = application.injector.instanceOf[ZeroEmissionCarAllowanceView]

        val rentalsRequest = FakeRequest(GET, rentalsZeroEmissionCarAllowanceRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, taxYear, individual, NormalMode, Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomZeroEmissionCarAllowanceRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual OK
        contentAsString(rentalsRentARoomResult) mustEqual view(form, taxYear, individual, NormalMode, RentalsRentARoom)(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered both for the rentals and the combined journey" in {

      val rentalsUserAnswers =
        UserAnswers(userAnswersId).set(ZeroEmissionCarAllowancePage(Rentals), validAnswer).success.value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), isAgent = false).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsZeroEmissionCarAllowanceRoute)

        val view = rentalsApplication.injector.instanceOf[ZeroEmissionCarAllowanceView]

        val result = route(rentalsApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, individual, NormalMode, Rentals)(
          request,
          messages(rentalsApplication)
        ).toString

      }

      val rentalsRentARoomUserAnswers =
        UserAnswers(userAnswersId).set(ZeroEmissionCarAllowancePage(RentalsRentARoom), validAnswer).success.value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), isAgent = false).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomZeroEmissionCarAllowanceRoute)

        val view = rentalsRentARoomApplication.injector.instanceOf[ZeroEmissionCarAllowanceView]

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(validAnswer),
          taxYear,
          individual,
          NormalMode,
          RentalsRentARoom
        )(
          request,
          messages(rentalsRentARoomApplication)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted both for the rentals and the combined journey" in {

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
          FakeRequest(POST, rentalsZeroEmissionCarAllowanceRoute)
            .withFormUrlEncodedBody(("zeroEmissionCarAllowanceAmount", validAnswer.toString))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual onwardRoute.url

        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomZeroEmissionCarAllowanceRoute)
            .withFormUrlEncodedBody(("zeroEmissionCarAllowanceAmount", validAnswer.toString))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted both for the rentals and the combined journey" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val rentalsRequest =
          FakeRequest(POST, rentalsZeroEmissionCarAllowanceRoute)
            .withFormUrlEncodedBody(("zeroEmissionCarAllowanceAmount", "invalid value"))

        val boundForm = form.bind(Map("zeroEmissionCarAllowanceAmount" -> "invalid value"))

        val view = application.injector.instanceOf[ZeroEmissionCarAllowanceView]

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(boundForm, taxYear, individual, NormalMode, Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomZeroEmissionCarAllowanceRoute)
            .withFormUrlEncodedBody(("zeroEmissionCarAllowanceAmount", "invalid value"))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual BAD_REQUEST
        contentAsString(rentalsRentARoomResult) mustEqual view(
          boundForm,
          taxYear,
          individual,
          NormalMode,
          RentalsRentARoom
        )(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found both for the rentals and the combined journey" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val rentalsRequest = FakeRequest(GET, rentalsZeroEmissionCarAllowanceRoute)

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomZeroEmissionCarAllowanceRoute)

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found both for the rentals and the combined journey" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val rentalsRequest =
          FakeRequest(POST, rentalsZeroEmissionCarAllowanceRoute)
            .withFormUrlEncodedBody(("zeroEmissionCarAllowanceAmount", validAnswer.toString))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER

        redirectLocation(rentalsResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomZeroEmissionCarAllowanceRoute)
            .withFormUrlEncodedBody(("zeroEmissionCarAllowanceAmount", validAnswer.toString))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER

        redirectLocation(rentalsRentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
