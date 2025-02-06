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
import forms.ukandforeignproperty.UkAndForeignBalancingChargeFormProvider
import models.{BalancingCharge, NormalMode, UserAnswers}
import navigation.{FakeUKAndForeignPropertyNavigator, UkAndForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.ForeignUkandForeignBalancingChargePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukandforeignproperty.UkAndForeignBalancingChargeView

import scala.concurrent.Future

class UkAndForeignBalancingChargeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val taxYear: Int = 2024
  val isAgentMessageKey: String = "individual"
  val formProvider: UkAndForeignBalancingChargeFormProvider = new UkAndForeignBalancingChargeFormProvider()
  val form: Form[BalancingCharge] = formProvider(isAgentMessageKey)

  lazy val ukAndForeignBalancingChargeRoute: String = controllers.ukandforeignproperty.routes.UkAndForeignBalancingChargeController.onPageLoad(taxYear, NormalMode).url

  "UkAndForeignBalancingCharge Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, ukAndForeignBalancingChargeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UkAndForeignBalancingChargeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, isAgentMessageKey, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ForeignUkandForeignBalancingChargePage, BalancingCharge(true, Some(BigDecimal(123.23)))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, ukAndForeignBalancingChargeRoute)

        val view = application.injector.instanceOf[UkAndForeignBalancingChargeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(BalancingCharge(true, Some(BigDecimal(123.23)))),taxYear, isAgentMessageKey, NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, ukAndForeignBalancingChargeRoute)
            .withFormUrlEncodedBody(("balancingChargeYesNo", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, ukAndForeignBalancingChargeRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UkAndForeignBalancingChargeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageKey, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, ukAndForeignBalancingChargeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, ukAndForeignBalancingChargeRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
