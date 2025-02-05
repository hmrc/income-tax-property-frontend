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
import forms.ukandforeignproperty.BalancingChargeFormProvider
import models.{BalancingCharge, NormalMode, UserAnswers}
import navigation.{UkAndForeignPropertyNavigator, FakeUKAndForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.UkBalancingChargePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukandforeignproperty.BalancingChargeView

import scala.concurrent.Future

class BalancingChargeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/update-and-submit-income-tax-return/property")

  val taxYear: Int = 2024
  lazy val balancingChargeRoute: String =
    controllers.ukandforeignproperty.routes.BalancingChargeController.onPageLoad(taxYear, NormalMode).url

  val formProvider = new BalancingChargeFormProvider()
  private val isAgentMessageKey = "individual"
  val form: Form[BalancingCharge] = formProvider(isAgentMessageKey)

  "ForeignBalancingCharge Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val view = application.injector.instanceOf[BalancingChargeView]

      running(application) {
        val request = FakeRequest(GET, balancingChargeRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, isAgentMessageKey)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(UkBalancingChargePage, BalancingCharge(balancingChargeYesNo = true, Some(7689.23)))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, balancingChargeRoute)
        val view = application.injector.instanceOf[BalancingChargeView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(BalancingCharge(balancingChargeYesNo = true, Some(7689.23))),
          taxYear,
          NormalMode,
          isAgentMessageKey
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[UkAndForeignPropertyNavigator].toInstance(new FakeUKAndForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, balancingChargeRoute)
            .withFormUrlEncodedBody("balancingChargeYesNo" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val boundForm = form.bind(Map("balancingChargeAmount" -> "87.858585"))
      val view = application.injector.instanceOf[BalancingChargeView]

      running(application) {

        val request =
          FakeRequest(POST, balancingChargeRoute)
            .withFormUrlEncodedBody(("balancingChargeAmount", "87.858585"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, isAgentMessageKey)(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
