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

package controllers.ukandforeignproperty

import base.SpecBase
import controllers.routes
import forms.ukandforeignproperty.income.UkAndForeignPropertyForeignOtherIncomeFromPropertyFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.ForeignOtherIncomeFromForeignPropertyPage
import play.api.Application
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukandforeignproperty.UkAndForeignPropertyForeignOtherIncomeFromPropertyView

import scala.concurrent.Future

class UkAndForeignPropertyForeignOtherIncomeFromPropertyControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val individualOrAgent: String = "individual"
  val taxYear: Int = 2024
  val otherIncomeFromProperty: BigDecimal = BigDecimal(12345)
  val formProvider = new UkAndForeignPropertyForeignOtherIncomeFromPropertyFormProvider()
  val form: Form[BigDecimal] = formProvider(individualOrAgent)

  lazy val foreignOtherIncomeFromPropertyRoute =
    controllers.ukandforeignproperty.routes.UkAndForeignPropertyForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, NormalMode).url

  "UkAndForeignPropertyForeignOtherIncomeFromProperty Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignOtherIncomeFromPropertyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UkAndForeignPropertyForeignOtherIncomeFromPropertyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, individualOrAgent)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ForeignOtherIncomeFromForeignPropertyPage, otherIncomeFromProperty).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignOtherIncomeFromPropertyRoute)

        val view = application.injector.instanceOf[UkAndForeignPropertyForeignOtherIncomeFromPropertyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(otherIncomeFromProperty),
          taxYear,
          NormalMode,
          individualOrAgent)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignOtherIncomeFromPropertyRoute)
            .withFormUrlEncodedBody(("otherPropertyIncome", otherIncomeFromProperty.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        val redirect = redirectLocation(result)
        redirect.value mustEqual controllers.ukandforeignproperty.routes.PropertyIncomeAllowanceClaimController.onPageLoad(taxYear, NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignOtherIncomeFromPropertyRoute)
            .withFormUrlEncodedBody(("otherPropertyIncome", "invalid value"))

        val boundForm = form.bind(Map("otherPropertyIncome" -> "invalid value"))

        val view = application.injector.instanceOf[UkAndForeignPropertyForeignOtherIncomeFromPropertyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, individualOrAgent)(request, messages(application)).toString
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
