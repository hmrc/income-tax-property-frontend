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

package controllers.foreign.income

import base.SpecBase
import controllers.routes
import forms.foreign.income.ForeignOtherIncomeFromPropertyFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.income.ForeignOtherIncomeFromPropertyPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.income.ForeignOtherIncomeFromPropertyView

import scala.concurrent.Future

class ForeignOtherIncomeFromPropertyControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new ForeignOtherIncomeFromPropertyFormProvider()
  val form: Form[BigDecimal] = formProvider("individual")
  val taxYear: Int = 2024
  val countryCode: String = "AUS"
  val individualOrAgent: String = "individual"
  val otherIncomeFromProperty: BigDecimal = BigDecimal(12345)

  lazy val foreignOtherIncomeFromPropertyRoute =
    controllers.foreign.income.routes.ForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, countryCode, NormalMode).url

  "ForeignOtherIncomeFromProperty Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignOtherIncomeFromPropertyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignOtherIncomeFromPropertyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, countryCode, NormalMode, individualOrAgent)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ForeignOtherIncomeFromPropertyPage(countryCode), otherIncomeFromProperty).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignOtherIncomeFromPropertyRoute)

        val view = application.injector.instanceOf[ForeignOtherIncomeFromPropertyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(otherIncomeFromProperty),
          taxYear,
          countryCode,
          NormalMode,
          individualOrAgent)(request, messages(application)).toString
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
          FakeRequest(POST, foreignOtherIncomeFromPropertyRoute)
            .withFormUrlEncodedBody(("otherPropertyIncome", otherIncomeFromProperty.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignOtherIncomeFromPropertyRoute)
            .withFormUrlEncodedBody(("otherPropertyIncome", "invalid value"))

        val boundForm = form.bind(Map("otherPropertyIncome" -> "invalid value"))

        val view = application.injector.instanceOf[ForeignOtherIncomeFromPropertyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, countryCode, NormalMode, individualOrAgent)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, foreignOtherIncomeFromPropertyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignOtherIncomeFromPropertyRoute)
            .withFormUrlEncodedBody(("otherPropertyIncome", otherIncomeFromProperty.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
