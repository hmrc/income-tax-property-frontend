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

package controllers.ukandforeignproperty

import base.SpecBase
import forms.ukandforeignproperty.RemoveCountryFormProvider
import models.{Index, NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.Country
import pages.ukandforeignproperty.SelectCountryPage
import play.api.Application
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ukandforeignproperty.RemoveCountryView

class RemoveCountryControllerSpec extends SpecBase with MockitoSugar {

  val testTaxYear: Int = 2024
  val testIndex: Index = Index(1)
  val testCountry: Country = Country("France", "FR")
  val formProvider = new RemoveCountryFormProvider()
  val form: Form[Boolean] = formProvider()
  val testAnswers: UserAnswers = emptyUserAnswers.set(SelectCountryPage, List(testCountry)).success.value

  "GET" - {
    "Return OK when a valid index has been requested" in {
      val application: Application = applicationBuilder(userAnswers = Some(testAnswers), isAgent = false).build()

      running(application) {
        val controller = application.injector.instanceOf[RemoveCountryController]
        val view = application.injector.instanceOf[RemoveCountryView]

        val result = controller.onPageLoad(testTaxYear, testIndex, NormalMode)(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustBe view(form, testTaxYear, NormalMode, testIndex, testCountry)(FakeRequest(), messages(application)).toString
      }
    }

    "return NOT_FOUND when an invalid index has been requested" in {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val controller = application.injector.instanceOf[RemoveCountryController]

        val result = controller.onPageLoad(testTaxYear, testIndex, NormalMode)(FakeRequest())

        status(result) mustBe NOT_FOUND
      }
    }
  }

  "POST" - {
    "onSubmit must redirect to ForeignCountriesRentedController when valid data is provided" in {
      val application: Application = applicationBuilder(userAnswers = Some(testAnswers), isAgent = false).build()

      running(application) {
        val controller = application.injector.instanceOf[RemoveCountryController]
        val result = controller.onSubmit(testTaxYear, testIndex, NormalMode)(FakeRequest(POST, "/").withFormUrlEncodedBody(("value", "true")))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(routes.ForeignCountriesRentedController.onPageLoad(taxYear = testTaxYear, mode = NormalMode).url)
      }
    }

    s"must return a Bad Request and errors when no data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(testAnswers), isAgent = false).build()

      running(application) {
        val controller = application.injector.instanceOf[RemoveCountryController]
        val result = controller.onSubmit(testTaxYear, testIndex, NormalMode)(FakeRequest(POST, "/").withFormUrlEncodedBody(("value", " ")))

        val boundForm = form.bind(Map("value" -> " "))

        val view = application.injector.instanceOf[RemoveCountryView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustBe view(boundForm, testTaxYear, NormalMode, testIndex, testCountry)(FakeRequest(), messages(application)).toString
      }
    }

    "onSubmit must fail and throw an exception when invalid index data is provided" in {
      val application: Application = applicationBuilder(userAnswers = Some(testAnswers), isAgent = false).build()
      running(application) {
        val invalidIndex = Index(2)
        val controller = application.injector.instanceOf[RemoveCountryController]

        val request = FakeRequest(POST, "/")
        val result = controller.onSubmit(testTaxYear, invalidIndex, NormalMode)(request)

        status(result) mustBe NOT_FOUND
      }
    }

    "must redirect to Journey Recovery when no existing data is found" in {
      val application: Application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val controller = application.injector.instanceOf[RemoveCountryController]
        val result = controller.onSubmit(testTaxYear, testIndex, NormalMode)(FakeRequest(POST, "/"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
