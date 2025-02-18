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
import forms.foreign.SelectIncomeCountryFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.{Country, SelectIncomeCountryPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.CountryNamesDataSource.{countrySelectItems, countrySelectItemsWithUSA}
import views.html.foreign.SelectIncomeCountryView

import scala.concurrent.Future

class SelectIncomeCountryControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call =
    Call("GET", "/update-and-submit-income-tax-return/property/2024/foreign-property/countries-rented-property")
  val taxYear = 2024
  val country: Country = Country(name = "India", code = "IND")
  val index = 0
  val userType = "agent"
  val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(SelectIncomeCountryPage(index), country).success.value
  val formProvider = new SelectIncomeCountryFormProvider()
  val form: Form[String] = formProvider(userType, userAnswers)

  lazy val selectIncomeCountryRoute: String =
    routes.SelectIncomeCountryController.onPageLoad(taxYear, index, NormalMode).url

  "SelectIncomeCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, selectIncomeCountryRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectIncomeCountryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, index, userType, NormalMode, countrySelectItemsWithUSA("en"))(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, selectIncomeCountryRoute)

        val view = application.injector.instanceOf[SelectIncomeCountryView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill("IND"),
          taxYear,
          index,
          userType,
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
          FakeRequest(POST, selectIncomeCountryRoute)
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
          FakeRequest(POST, selectIncomeCountryRoute)
            .withFormUrlEncodedBody(("country-autocomplete", ""))

        val boundForm = form.bind(Map("country-autocomplete" -> ""))

        val view = application.injector.instanceOf[SelectIncomeCountryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, index, userType, NormalMode, countrySelectItems("en"))(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, selectIncomeCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, selectIncomeCountryRoute)
            .withFormUrlEncodedBody(("country-autocomplete", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
