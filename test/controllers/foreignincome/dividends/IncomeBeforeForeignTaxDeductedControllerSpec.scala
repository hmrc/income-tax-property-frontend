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

package controllers.foreignincome.dividends

import base.SpecBase
import forms.foreignincome.dividends.IncomeBeforeForeignTaxDeductedFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeForeignIncomeNavigator, ForeignIncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.Country
import pages.foreignincome.IncomeBeforeForeignTaxDeductedPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreignincome.dividends.IncomeBeforeForeignTaxDeductedView

import scala.concurrent.Future

class IncomeBeforeForeignTaxDeductedControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new IncomeBeforeForeignTaxDeductedFormProvider()
  private val isAgentMessageKey = "individual"
  val form: Form[BigDecimal] = formProvider()

  def onwardRoute: Call = Call("GET", "/foo")

  val validAnswer: BigDecimal = BigDecimal(0)
  val taxYear = 2023
  val country: Country = Country("United States of America", "USA")

  lazy val incomeBeforeForeignTaxDeductedRoute: String = routes.IncomeBeforeForeignTaxDeductedController.onPageLoad(taxYear, country.code, NormalMode).url

  "IncomeBeforeForeignTaxDeducted Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, incomeBeforeForeignTaxDeductedRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IncomeBeforeForeignTaxDeductedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, country, isAgentMessageKey, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IncomeBeforeForeignTaxDeductedPage(country.code), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, incomeBeforeForeignTaxDeductedRoute)

        val view = application.injector.instanceOf[IncomeBeforeForeignTaxDeductedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, country, isAgentMessageKey, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[ForeignIncomeNavigator].toInstance(new FakeForeignIncomeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, incomeBeforeForeignTaxDeductedRoute)
            .withFormUrlEncodedBody(("incomeBeforeForeignTaxDeducted", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, incomeBeforeForeignTaxDeductedRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[IncomeBeforeForeignTaxDeductedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, country, isAgentMessageKey, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, incomeBeforeForeignTaxDeductedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, incomeBeforeForeignTaxDeductedRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
