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
import controllers.routes
import forms.foreign.CalculatedPremiumLeaseTaxableFormProvider
import models.{NormalMode, PremiumCalculated, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.CalculatedPremiumLeaseTaxablePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.CalculatedPremiumLeaseTaxableView

import scala.concurrent.Future

class CalculatedPremiumLeaseTaxableControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")
  val isAgentMessageKey = "individual"
  val formProvider = new CalculatedPremiumLeaseTaxableFormProvider()
  val form: Form[PremiumCalculated] = formProvider(isAgentMessageKey)
  val taxYear: Int = 2024
  val countryCode: String = "AUS"

  lazy val calculatedPremiumLeaseTaxableRoute: String =
    controllers.foreign.routes.CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, NormalMode).url

  "CalculatedPremiumLeaseTaxable Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, calculatedPremiumLeaseTaxableRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CalculatedPremiumLeaseTaxableView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, countryCode, NormalMode, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val amount = 100
      val formAnswers = PremiumCalculated(
        calculatedPremiumLeaseTaxable = true,
        Some(amount)
      )
      val userAnswers = UserAnswers(userAnswersId).set(
        CalculatedPremiumLeaseTaxablePage(countryCode),
        formAnswers
      ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, calculatedPremiumLeaseTaxableRoute)

        val view = application.injector.instanceOf[CalculatedPremiumLeaseTaxableView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(formAnswers), taxYear, countryCode ,NormalMode, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted for yes" in {

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
          FakeRequest(POST, calculatedPremiumLeaseTaxableRoute)
            .withFormUrlEncodedBody(("calculatedPremiumLeaseTaxable", "false"), ("premiumsOfLeaseGrant","100"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, calculatedPremiumLeaseTaxableRoute)
            .withFormUrlEncodedBody(("calculatedPremiumLeaseTaxable", ""))

        val boundForm = form.bind(Map("calculatedPremiumLeaseTaxable" -> ""))

        val view = application.injector.instanceOf[CalculatedPremiumLeaseTaxableView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, countryCode, NormalMode, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, calculatedPremiumLeaseTaxableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, calculatedPremiumLeaseTaxableRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
