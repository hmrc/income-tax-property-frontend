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
import controllers.routes
import forms.foreignincome.dividends.ForeignTaxDeductedFromDividendIncomeFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeForeignIncomeNavigator, ForeignIncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.Country
import pages.foreignincome.dividends.ForeignTaxDeductedFromDividendIncomePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreignincome.dividends.ForeignTaxDeductedFromDividendIncomeView

import scala.concurrent.Future

class ForeignTaxDeductedFromDividendIncomeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ForeignTaxDeductedFromDividendIncomeFormProvider()
  val taxYear: Int = 2024
  val country: Country = Country("Australia","AUS")
  val isAgentMessageString: String = "individual"
  val form: Form[Boolean] = formProvider(isAgentMessageString)
  lazy val foreignTaxDeductedFromDividendIncomeRoute = controllers.foreignincome.dividends.routes.ForeignTaxDeductedFromDividendIncomeController.onPageLoad(taxYear, country.code, NormalMode).url

  "ForeignTaxDeductedFromDividendIncome Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignTaxDeductedFromDividendIncomeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignTaxDeductedFromDividendIncomeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, isAgentMessageString, country, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ForeignTaxDeductedFromDividendIncomePage(country.code), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignTaxDeductedFromDividendIncomeRoute)

        val view = application.injector.instanceOf[ForeignTaxDeductedFromDividendIncomeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), taxYear, isAgentMessageString, country, NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, foreignTaxDeductedFromDividendIncomeRoute)
            .withFormUrlEncodedBody(("foreignTaxDeductedFromDividendIncome", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignTaxDeductedFromDividendIncomeRoute)
            .withFormUrlEncodedBody(("foreignTaxDeductedFromDividendIncome", ""))

        val boundForm = form.bind(Map("foreignTaxDeductedFromDividendIncome" -> ""))

        val view = application.injector.instanceOf[ForeignTaxDeductedFromDividendIncomeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageString, country, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, foreignTaxDeductedFromDividendIncomeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignTaxDeductedFromDividendIncomeRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
