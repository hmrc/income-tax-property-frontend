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

package controllers.ukandforeignproperty

import base.SpecBase
import controllers.routes
import forms.ukandforeignproperty.UkAndForeignForeignPremiumsForTheGrantOfALeaseFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeUKAndForeignPropertyNavigator, UkAndForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.ForeignPremiumsForTheGrantOfALeasePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukandforeignproperty.UkAndForeignForeignPremiumsForTheGrantOfALeaseView

import scala.concurrent.Future

class UkAndForeignForeignPremiumsForTheGrantOfALeaseControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new UkAndForeignForeignPremiumsForTheGrantOfALeaseFormProvider()
  val isAgentMessageKey: String = "individual"
  val taxYear: Int = 2024
  val form = formProvider(isAgentMessageKey)

  lazy val ukAndForeignForeignPremiumsForTheGrantOfALeaseRoute = controllers.ukandforeignproperty.routes.UkAndForeignForeignPremiumsForTheGrantOfALeaseController.onPageLoad(taxYear, NormalMode).url

  "UkAndForeignForeignPremiumsForTheGrantOfALease Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, ukAndForeignForeignPremiumsForTheGrantOfALeaseRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UkAndForeignForeignPremiumsForTheGrantOfALeaseView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, isAgentMessageKey, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ForeignPremiumsForTheGrantOfALeasePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, ukAndForeignForeignPremiumsForTheGrantOfALeaseRoute)

        val view = application.injector.instanceOf[UkAndForeignForeignPremiumsForTheGrantOfALeaseView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), taxYear, isAgentMessageKey, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[UkAndForeignPropertyNavigator].toInstance(new FakeUKAndForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, ukAndForeignForeignPremiumsForTheGrantOfALeaseRoute)
            .withFormUrlEncodedBody(("ukAndForeignForeignPremiumsGrantLease", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, ukAndForeignForeignPremiumsForTheGrantOfALeaseRoute)
            .withFormUrlEncodedBody(("ukAndForeignForeignPremiumsGrantLease", ""))

        val boundForm = form.bind(Map("ukAndForeignForeignPremiumsGrantLease" -> ""))

        val view = application.injector.instanceOf[UkAndForeignForeignPremiumsForTheGrantOfALeaseView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageKey, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, ukAndForeignForeignPremiumsForTheGrantOfALeaseRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, ukAndForeignForeignPremiumsForTheGrantOfALeaseRoute)
            .withFormUrlEncodedBody(("ukAndForeignForeignPremiumsGrantLease", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
