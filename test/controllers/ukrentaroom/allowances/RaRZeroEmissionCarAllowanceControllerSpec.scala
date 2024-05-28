/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.ukrentaroom.allowances

import base.SpecBase
import forms.ukrentaroom.allowances.RaRZeroEmissionCarAllowanceFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.allowances.RaRZeroEmissionCarAllowancePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.allowances.RaRZeroEmissionCarAllowanceView

import scala.concurrent.Future

class RaRZeroEmissionCarAllowanceControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new RaRZeroEmissionCarAllowanceFormProvider()
  val form = formProvider("individual")

  def onwardRoute: Call = Call("GET", "/foo")

  private val taxYear = 2023
  private val zeroEmissionCarAllowance = 100
  private val validAnswer = BigDecimal.valueOf(zeroEmissionCarAllowance)
  private lazy val zeroEmissionCarAllowanceRoute =
    routes.RaRZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode).url

  "RaRZeroEmissionCarAllowance Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, zeroEmissionCarAllowanceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RaRZeroEmissionCarAllowanceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, "individual", NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(RaRZeroEmissionCarAllowancePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, zeroEmissionCarAllowanceRoute)

        val view = application.injector.instanceOf[RaRZeroEmissionCarAllowanceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, "individual", NormalMode)(
          request,
          messages(application)
        ).toString
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
          FakeRequest(POST, zeroEmissionCarAllowanceRoute)
            .withFormUrlEncodedBody(("zeroEmissionCarAllowance", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, zeroEmissionCarAllowanceRoute)
            .withFormUrlEncodedBody(("zeroEmissionCarAllowance", "invalid value"))

        val boundForm = form.bind(Map("zeroEmissionCarAllowance" -> "invalid value"))

        val view = application.injector.instanceOf[RaRZeroEmissionCarAllowanceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, "individual", NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, false).build()

      running(application) {
        val request = FakeRequest(GET, zeroEmissionCarAllowanceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, false).build()

      running(application) {
        val request =
          FakeRequest(POST, zeroEmissionCarAllowanceRoute)
            .withFormUrlEncodedBody(("zeroEmissionCarAllowance", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
