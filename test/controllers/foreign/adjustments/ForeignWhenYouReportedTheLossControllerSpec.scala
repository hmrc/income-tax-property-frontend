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

package controllers.foreign.adjustments

import base.SpecBase
import controllers.routes
import forms.foreign.adjustments.ForeignWhenYouReportedTheLossFormProvider
import models.{ForeignWhenYouReportedTheLoss, NormalMode, UnusedLossesPreviousYears, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.adjustments.{ForeignUnusedLossesPreviousYearsPage, ForeignWhenYouReportedTheLossPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.adjustments.ForeignWhenYouReportedTheLossView

import scala.concurrent.Future
import scala.math.BigDecimal.RoundingMode

class ForeignWhenYouReportedTheLossControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val foreignWhenYouReportedTheLossRoute: String = controllers.foreign.adjustments.routes.ForeignWhenYouReportedTheLossController.onPageLoad(taxYear, countryCode, NormalMode).url
  val formProvider = new ForeignWhenYouReportedTheLossFormProvider()
  val taxYear: Int = 2024
  val countryCode: String = "AUS"
  val isAgentMessageString: String = "individual"
  val previousLoss: BigDecimal = 123
  val form: Form[ForeignWhenYouReportedTheLoss] = formProvider(isAgentMessageString)

  "ForeignWhenYouReportedTheLoss Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers =
        UserAnswers("test").set(ForeignUnusedLossesPreviousYearsPage(countryCode),
          UnusedLossesPreviousYears(
            isUnusedLossesPreviousYears = true,
            Some(previousLoss)
          )
        ).get

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignWhenYouReportedTheLossRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignWhenYouReportedTheLossView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, countryCode, isAgentMessageString, previousLoss.setScale(2, RoundingMode.DOWN).toString, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ForeignWhenYouReportedTheLossPage(countryCode), ForeignWhenYouReportedTheLoss.values.head).success.value
      val userAnswersWithLoss = userAnswers.set(ForeignUnusedLossesPreviousYearsPage(countryCode),
        UnusedLossesPreviousYears(
          isUnusedLossesPreviousYears = true,
          Some(previousLoss)
        )
      ).get

      val application = applicationBuilder(userAnswers = Some(userAnswersWithLoss), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignWhenYouReportedTheLossRoute)

        val view = application.injector.instanceOf[ForeignWhenYouReportedTheLossView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(ForeignWhenYouReportedTheLoss.values.head), taxYear, countryCode, isAgentMessageString, previousLoss.setScale(2, RoundingMode.DOWN).toString, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswers =
        UserAnswers("test").set(ForeignUnusedLossesPreviousYearsPage(countryCode),
          UnusedLossesPreviousYears(
            isUnusedLossesPreviousYears = true,
            Some(previousLoss)
          )
        ).get
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), isAgent = false)
          .overrides(
            bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, foreignWhenYouReportedTheLossRoute)
            .withFormUrlEncodedBody(("whenYouReportedTheLoss", ForeignWhenYouReportedTheLoss.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers =
        UserAnswers("test").set(ForeignUnusedLossesPreviousYearsPage(countryCode),
          UnusedLossesPreviousYears(
            isUnusedLossesPreviousYears = true,
            Some(previousLoss)
          )
        ).get
      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()
      running(application) {
        val request =
          FakeRequest(POST, foreignWhenYouReportedTheLossRoute)
            .withFormUrlEncodedBody(("whenYouReportedTheLoss", "invalid value"))

        val boundForm = form.bind(Map("whenYouReportedTheLoss" -> "invalid value"))

        val view = application.injector.instanceOf[ForeignWhenYouReportedTheLossView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, countryCode, isAgentMessageString, previousLoss.setScale(2, RoundingMode.DOWN).toString, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignWhenYouReportedTheLossRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignWhenYouReportedTheLossRoute)
            .withFormUrlEncodedBody(("whenYouReportedTheLoss", ForeignWhenYouReportedTheLoss.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
