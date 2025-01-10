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
import forms.foreign.adjustments.ForeignUnusedLossesPreviousYearsFormProvider
import models.{NormalMode, UnusedLossesPreviousYears, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, FakeNavigator, ForeignPropertyNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.adjustments.ForeignUnusedLossesPreviousYearsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.adjustments.ForeignUnusedLossesPreviousYearsView

import scala.concurrent.Future

class ForeignUnusedLossesPreviousYearsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val taxYear = 2024
  val countryCode = "AUS"
  val isAgent = "individual"
  val formProvider = new ForeignUnusedLossesPreviousYearsFormProvider()
  val form: Form[UnusedLossesPreviousYears] = formProvider(isAgent)
  lazy val foreignUnusedLossesPreviousYearsRoute = controllers.foreign.adjustments.routes.ForeignUnusedLossesPreviousYearsController.onPageLoad(taxYear, countryCode, NormalMode).url

  "ForeignUnusedLossesPreviousYears Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignUnusedLossesPreviousYearsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignUnusedLossesPreviousYearsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form,taxYear,countryCode, isAgent, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val amount = 100
      val formAnswers = UnusedLossesPreviousYears(
        unusedLossesPreviousYearsYesNo = true,
        Some(amount)
      )
      val userAnswers = UserAnswers(userAnswersId).set(ForeignUnusedLossesPreviousYearsPage(countryCode), formAnswers).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignUnusedLossesPreviousYearsRoute)

        val view = application.injector.instanceOf[ForeignUnusedLossesPreviousYearsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(formAnswers), taxYear, countryCode, isAgent, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, foreignUnusedLossesPreviousYearsRoute)
            .withFormUrlEncodedBody(("unusedLossesPreviousYearsYesNo", "true"), ("unusedLossesPreviousYearsAmount", "100"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignUnusedLossesPreviousYearsRoute)
            .withFormUrlEncodedBody(("unusedLossesPreviousYearsYesNo", ""))

        val boundForm = form.bind(Map("unusedLossesPreviousYearsYesNo" -> ""))

        val view = application.injector.instanceOf[ForeignUnusedLossesPreviousYearsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, countryCode, isAgent, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, foreignUnusedLossesPreviousYearsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignUnusedLossesPreviousYearsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
