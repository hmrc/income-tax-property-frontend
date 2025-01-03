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
import models.{Index, NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.Country
import pages.ukandforeignproperty.SelectCountryPage
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ukandforeignproperty.RemoveCountryView

import scala.util.Try

class RemoveCountryControllerSpec extends SpecBase with MockitoSugar {

  val testTaxYear: Int = 2024
  val testIndex: Index = Index(1)
  val testCountry: Country = Country("France", "FR")
  val testAnswers: UserAnswers = emptyUserAnswers.set(SelectCountryPage, Set(testCountry)).success.value

  "GET" - {
    "Return OK when a valid index has been requested" in {
      val application: Application = applicationBuilder(userAnswers = Some(testAnswers), isAgent = false).build()

      running(application) {
        val controller = application.injector.instanceOf[RemoveCountryController]
        val view = application.injector.instanceOf[RemoveCountryView]

        val result = controller.onPageLoad(testTaxYear, testIndex, NormalMode)(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustBe view(testTaxYear, NormalMode, testIndex, testCountry)(FakeRequest(), messages(application)).toString
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
        val result = controller.onSubmit(testTaxYear, testIndex, NormalMode)(FakeRequest(POST, "/"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(routes.ForeignCountriesRentedController.onPageLoad(taxYear = testTaxYear, mode = NormalMode).url)
      }
    }

    "onSubmit must fail and throw an exception when invalid index data is provided" in {
      val application: Application = applicationBuilder(userAnswers = Some(testAnswers), isAgent = false).build()
      running(application) {
        val invalidIndex = Index(2)
        val controller = application.injector.instanceOf[RemoveCountryController]

        val request = FakeRequest(POST, "/")
        val result = controller.onSubmit(testTaxYear, invalidIndex, NormalMode)(request)
        val s = Try{status(result)}
        s.isFailure mustBe true
        s.asInstanceOf[scala.util.Failure[String]].exception.getMessage mustBe s"No country exists for position index: ${invalidIndex.position}"
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
