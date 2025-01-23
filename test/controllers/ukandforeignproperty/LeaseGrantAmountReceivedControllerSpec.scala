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
import controllers.ukandforeignproperty.routes.LeaseGrantAmountReceivedController
import forms.ukandforeignproperty.LeaseGrantAmountReceivedFormProvider
import views.html.ukandforeignproperty.LeaseGrantAmountReceivedView
import models.{UserAnswers, NormalMode}
import navigation.{Navigator, FakeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.LeaseGrantAmountReceivedPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository

import scala.concurrent.Future

class LeaseGrantAmountReceivedControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new LeaseGrantAmountReceivedFormProvider()
  val form: Form[BigDecimal] = formProvider()
  val taxYear = 2024
  val countryCode = "GRC"

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = BigDecimal(100)

  lazy val leaseGrantAmountReceivedRoute = LeaseGrantAmountReceivedController.onPageLoad(taxYear, countryCode, NormalMode).url

  "LeaseGrantAmountReceived Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, leaseGrantAmountReceivedRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LeaseGrantAmountReceivedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, countryCode, "individual", NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(LeaseGrantAmountReceivedPage(countryCode), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, leaseGrantAmountReceivedRoute)

        val view = application.injector.instanceOf[LeaseGrantAmountReceivedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, countryCode, "individual", NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, leaseGrantAmountReceivedRoute)
            .withFormUrlEncodedBody(("leaseGrantReceivedAmount", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, leaseGrantAmountReceivedRoute)
            .withFormUrlEncodedBody(("leaseGrantReceivedAmount", "invalid value"))

        val boundForm = form.bind(Map("leaseGrantReceivedAmount" -> "invalid value"))

        val view = application.injector.instanceOf[LeaseGrantAmountReceivedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, countryCode, "individual", NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, leaseGrantAmountReceivedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, leaseGrantAmountReceivedRoute)
            .withFormUrlEncodedBody(("leaseGrantReceivedAmount", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
