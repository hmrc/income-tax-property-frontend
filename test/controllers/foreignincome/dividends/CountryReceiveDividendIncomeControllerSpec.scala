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
import forms.foreignincome.dividends.CountryReceiveDividendIncomeFormProvider
import models.{UserAnswers, NormalMode}
import navigation.{Navigator, FakeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreignincome.CountryReceiveDividendIncomePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import controllers.foreignincome.dividends.routes.CountryReceiveDividendIncomeController
import pages.foreign.Country
import service.CountryNamesDataSource.countrySelectItemsWithUSA
import views.html.foreignincome.dividends.CountryReceiveDividendIncomeView

import scala.concurrent.Future

class CountryReceiveDividendIncomeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/update-and-submit-income-tax-return/property/2024/dividends/country-receive-dividend-income")

  val taxYear = 2024
  val country: Country = Country(name = "India", code = "IND")
  val index = 0
  val userType = "agent"
  val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(CountryReceiveDividendIncomePage(index), country).success.value
  val formProvider = new CountryReceiveDividendIncomeFormProvider()
  val form = formProvider(userAnswers)

  lazy val countryReceiveDividendIncomeRoute = CountryReceiveDividendIncomeController.onPageLoad(taxYear, index, NormalMode).url

  "CountryReceiveDividendIncome Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, countryReceiveDividendIncomeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CountryReceiveDividendIncomeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, index, NormalMode, countrySelectItemsWithUSA("en"))(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, countryReceiveDividendIncomeRoute)

        val view = application.injector.instanceOf[CountryReceiveDividendIncomeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill("IND"),
          taxYear,
          index,
          NormalMode,
          countrySelectItemsWithUSA("en")
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, countryReceiveDividendIncomeRoute)
            .withFormUrlEncodedBody(("country-autocomplete", "AUS"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, countryReceiveDividendIncomeRoute)
            .withFormUrlEncodedBody(("country-autocomplete", ""))

        val boundForm = form.bind(Map("country-autocomplete" -> ""))

        val view = application.injector.instanceOf[CountryReceiveDividendIncomeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, index, NormalMode, countrySelectItemsWithUSA("en"))(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, countryReceiveDividendIncomeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, countryReceiveDividendIncomeRoute)
            .withFormUrlEncodedBody(("country-autocomplete", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
