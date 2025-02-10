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
import models.{NormalMode, UserAnswers, UnusedLossesBroughtForward}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
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
  lazy val unusedLossesBroughtForwardRoute: String = routes.UnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode).url
  val form = formProvider(isAgentMessageString)
  val validAnswer: UnusedLossesBroughtForward = UnusedLossesBroughtForward(
    unusedLossesBroughtForwardYesOrNo = true, unusedLossesBroughtForwardAmount = Some(123.45))

  s"UnusedLossesBroughtForward Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, unusedLossesBroughtForwardRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnusedLossesBroughtForwardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, isAgentMessageString, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(UnusedLossesBroughtForwardPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, unusedLossesBroughtForwardRoute)

        val view = application.injector.instanceOf[UnusedLossesBroughtForwardView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, isAgentMessageString, NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, unusedLossesBroughtForwardRoute)
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
          FakeRequest(POST, unusedLossesBroughtForwardRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UnusedLossesBroughtForwardView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageString, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, unusedLossesBroughtForwardRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, unusedLossesBroughtForwardRoute)
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