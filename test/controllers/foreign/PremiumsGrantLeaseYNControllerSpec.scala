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

package controllers.foreign

import base.SpecBase
import controllers.foreign.routes.PremiumsGrantLeaseYNController
import controllers.routes
import forms.PremiumsGrantLeaseYNFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.income.PremiumsGrantLeaseYNPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.PremiumsGrantLeaseYNView

import scala.concurrent.Future

class PremiumsGrantLeaseYNControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new PremiumsGrantLeaseYNFormProvider()
  val form = formProvider("agent")
  val countryCode = "gre"
  val taxYear = 2024
  val isAgent = "agent"

  private lazy val premiumsGrantLeaseYNRoute = PremiumsGrantLeaseYNController.onPageLoad(taxYear, countryCode, NormalMode).url

  def onwardRoute: Call = Call(
    "GET",
    "/update-and-submit-income-tax-return/property/2024/foreign-property/income/gre/calculated-premium-lease-taxable"
  )

  "PremiumsGrantLeaseYN Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseYNRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PremiumsGrantLeaseYNView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, countryCode, NormalMode, isAgent)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(PremiumsGrantLeaseYNPage(countryCode), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseYNRoute)

        val view = application.injector.instanceOf[PremiumsGrantLeaseYNView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), taxYear, countryCode, NormalMode, isAgent)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseYNRoute)
            .withFormUrlEncodedBody(("premiumsGrantLeaseReceived", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), true).build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseYNRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PremiumsGrantLeaseYNView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, countryCode, NormalMode, isAgent)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseYNRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseYNRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
