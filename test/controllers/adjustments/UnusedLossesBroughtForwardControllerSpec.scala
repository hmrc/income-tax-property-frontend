/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.adjustments.UnusedLossesBroughtForwardFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers, UnusedLossesBroughtForward}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustments.UnusedLossesBroughtForwardPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.adjustments.UnusedLossesBroughtForwardView

import scala.concurrent.Future

class UnusedLossesBroughtForwardControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val taxYear: Int = 2024
  val isAgentMessageString: String = "individual"
  val formProvider = new UnusedLossesBroughtForwardFormProvider()
  val form = formProvider(isAgentMessageString)

  lazy val rentalsRoute: String = routes.UnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode, Rentals).url
  lazy val rentalsRentARoomRoute: String = routes.UnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url


  val validAnswer: UnusedLossesBroughtForward = UnusedLossesBroughtForward(
    unusedLossesBroughtForwardYesOrNo = true, unusedLossesBroughtForwardAmount = Some(123.45))

  s"UnusedLossesBroughtForward Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnusedLossesBroughtForwardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, isAgentMessageString, NormalMode, Rentals)(request, messages(application)).toString
      }
    }

    "Rentals only journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(UnusedLossesBroughtForwardPage(Rentals), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRoute)

        val view = application.injector.instanceOf[UnusedLossesBroughtForwardView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, isAgentMessageString, NormalMode, Rentals)(request, messages(application)).toString
      }
    }

    "Rentals RaR journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(UnusedLossesBroughtForwardPage(Rentals), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRoute)

        val view = application.injector.instanceOf[UnusedLossesBroughtForwardView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, isAgentMessageString, NormalMode, Rentals)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

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
        val request =
          FakeRequest(POST, rentalsRoute)
            .withFormUrlEncodedBody(
              "unusedLossesBroughtForwardYesOrNo" -> "true",
              "unusedLossesBroughtForwardAmount" -> "123.45"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UnusedLossesBroughtForwardView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageString, NormalMode, Rentals)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRoute)
            .withFormUrlEncodedBody(
              "unusedLossesBroughtForwardYesOrNo" -> validAnswer.unusedLossesBroughtForwardYesOrNo.toString,
              "unusedLossesBroughtForwardAmount" -> validAnswer.unusedLossesBroughtForwardAmount.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}