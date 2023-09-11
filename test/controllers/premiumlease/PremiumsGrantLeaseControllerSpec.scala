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
import forms.premiumlease.PremiumsGrantLeaseFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.premiumlease.PremiumsGrantLeasePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.premiumlease.PremiumsGrantLeaseView

import java.time.LocalDate
import scala.concurrent.Future

class PremiumsGrantLeaseControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new PremiumsGrantLeaseFormProvider()
  val form = formProvider()
  private val taxYear = LocalDate.now.getYear


  def onwardRoute = Call("GET", "/foo")

  val validAnswer = 0

  lazy val premiumsGrantLeaseRoute = routes.PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode).url

  "PremiumsGrantLease Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PremiumsGrantLeaseView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(PremiumsGrantLeasePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseRoute)

        val view = application.injector.instanceOf[PremiumsGrantLeaseView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, premiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PremiumsGrantLeaseView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
