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

package controllers.ukrentaroom.adjustments

import base.SpecBase
import controllers.routes
import forms.adjustments.WhenYouReportedTheLossFormProvider
import models.{UserAnswers, NormalMode, WhenYouReportedTheLoss, UnusedLossesBroughtForward, RentARoom}
import navigation.{Navigator, FakeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustments.WhenYouReportedTheLossPage
import pages.ukrentaroom.adjustments.RaRUnusedLossesBroughtForwardPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.adjustments.WhenYouReportedTheLossView

import scala.concurrent.Future
import scala.math.BigDecimal.RoundingMode

class RarWhenYouReportedTheLossControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")
  val taxYear: Int = 2024
  val isAgentMessageString: String = "individual"
  lazy val rarWhenYouReportedTheLossRoute: String = controllers.adjustments.routes.WhenYouReportedTheLossController.onPageLoad(taxYear, NormalMode, RentARoom).url
  val previousLoss: BigDecimal = 123
  val formProvider = new WhenYouReportedTheLossFormProvider()
  val form = formProvider(isAgentMessageString)

  "RarWhenYouReportedTheLoss Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers("test").set(RaRUnusedLossesBroughtForwardPage,
        UnusedLossesBroughtForward(
          isUnusedLossesBroughtForward = true,
          unusedLossesBroughtForwardAmount = Some(previousLoss)
        )).get
      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rarWhenYouReportedTheLossRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhenYouReportedTheLossView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, isAgentMessageString, previousLoss.setScale(2, RoundingMode.DOWN).toString, NormalMode, RentARoom)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(WhenYouReportedTheLossPage(RentARoom), WhenYouReportedTheLoss.values.head).success.value
      val userAnswersWithLoss = userAnswers.set(RaRUnusedLossesBroughtForwardPage,
        UnusedLossesBroughtForward(
          isUnusedLossesBroughtForward = true,
          unusedLossesBroughtForwardAmount = Some(previousLoss)
        )).get

      val application = applicationBuilder(userAnswers = Some(userAnswersWithLoss), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rarWhenYouReportedTheLossRoute)

        val view = application.injector.instanceOf[WhenYouReportedTheLossView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(WhenYouReportedTheLoss.values.head), taxYear, isAgentMessageString, previousLoss.setScale(2, RoundingMode.DOWN).toString, NormalMode, RentARoom)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = UserAnswers("test").set(RaRUnusedLossesBroughtForwardPage,
        UnusedLossesBroughtForward(
          isUnusedLossesBroughtForward = true,
          unusedLossesBroughtForwardAmount = Some(previousLoss)
        )).get

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, rarWhenYouReportedTheLossRoute)
            .withFormUrlEncodedBody(("whenYouReportedTheLoss", WhenYouReportedTheLoss.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers("test").set(RaRUnusedLossesBroughtForwardPage,
        UnusedLossesBroughtForward(
          isUnusedLossesBroughtForward = true,
          unusedLossesBroughtForwardAmount = Some(previousLoss)
        )).get

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, rarWhenYouReportedTheLossRoute)
            .withFormUrlEncodedBody(("whenYouReportedTheLoss", "invalid value"))

        val boundForm = form.bind(Map("whenYouReportedTheLoss" -> "invalid value"))

        val view = application.injector.instanceOf[WhenYouReportedTheLossView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageString, previousLoss.setScale(2, RoundingMode.DOWN).toString,NormalMode, RentARoom)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, rarWhenYouReportedTheLossRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, rarWhenYouReportedTheLossRoute)
            .withFormUrlEncodedBody(("rarWhenYouReportedTheLoss", WhenYouReportedTheLoss.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
