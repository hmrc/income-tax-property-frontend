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
import forms.premiumlease.ReceivedGrantLeaseAmountFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.premiumlease.ReceivedGrantLeaseAmountPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.premiumlease.ReceivedGrantLeaseAmountView

import java.time.LocalDate
import scala.concurrent.Future

class ReceivedGrantLeaseAmountControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new ReceivedGrantLeaseAmountFormProvider()
  private val form = formProvider("individual")
  private val taxYear = LocalDate.now.getYear
  private val validAnswer = BigDecimal(100)
  private def onwardRoute = Call("GET", "/year-lease-amount")

  "For Rentals ReceivedGrantLeaseAmount Controller" - {

    lazy val rentalsReceivedGrantLeaseAmountRoute =
      routes.ReceivedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode, Rentals).url

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsReceivedGrantLeaseAmountRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReceivedGrantLeaseAmountView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual", Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ReceivedGrantLeaseAmountPage(Rentals), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsReceivedGrantLeaseAmountRoute)

        val view = application.injector.instanceOf[ReceivedGrantLeaseAmountView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, NormalMode, "individual", Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is POSTed" in {

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
          FakeRequest(POST, rentalsReceivedGrantLeaseAmountRoute)
            .withFormUrlEncodedBody(("receivedGrantLeaseAmount", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is POSTed by an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsReceivedGrantLeaseAmountRoute)
            .withFormUrlEncodedBody(("receivedGrantLeaseAmount", "invalid value"))

        val boundForm = form.bind(Map("receivedGrantLeaseAmount" -> "invalid value"))

        val view = application.injector.instanceOf[ReceivedGrantLeaseAmountView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "agent", Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, rentalsReceivedGrantLeaseAmountRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsReceivedGrantLeaseAmountRoute)
            .withFormUrlEncodedBody(("receivedGrantLeaseAmount", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "For RentalsRentARoom ReceivedGrantLeaseAmount Controller" - {

    lazy val rentalsRentARoomGrantLeaseAmountRoute =
      routes.ReceivedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRentARoomGrantLeaseAmountRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReceivedGrantLeaseAmountView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual", RentalsRentARoom)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered by an agent" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(ReceivedGrantLeaseAmountPage(RentalsRentARoom), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRentARoomGrantLeaseAmountRoute)

        val view = application.injector.instanceOf[ReceivedGrantLeaseAmountView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, NormalMode, "agent", RentalsRentARoom)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is POSTed" in {

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
          FakeRequest(POST, rentalsRentARoomGrantLeaseAmountRoute)
            .withFormUrlEncodedBody(("receivedGrantLeaseAmount", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is POSTed by an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRentARoomGrantLeaseAmountRoute)
            .withFormUrlEncodedBody(("receivedGrantLeaseAmount", "invalid value"))

        val boundForm = form.bind(Map("receivedGrantLeaseAmount" -> "invalid value"))

        val view = application.injector.instanceOf[ReceivedGrantLeaseAmountView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "agent", RentalsRentARoom)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRentARoomGrantLeaseAmountRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRentARoomGrantLeaseAmountRoute)
            .withFormUrlEncodedBody(("receivedGrantLeaseAmount", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
