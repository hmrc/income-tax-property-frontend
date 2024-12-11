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
import forms.ukandforeignproperty.ReportIncomeFormProvider
import models.{NormalMode, ReportIncome, UserAnswers}
import navigation.{FakeUKAndForeignPropertyNavigator, UkAndForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.ReportIncomePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukandforeignproperty.ReportIncomeView

import scala.concurrent.Future

class ReportIncomeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")
  val taxYear = 2024

  lazy val reportIncomeRoute: String = routes.ReportIncomeController.onPageLoad(taxYear, NormalMode).url

  val formProvider = new ReportIncomeFormProvider()


  "ReportIncome Controller" - {

    Seq(("individual", false), ("agent", true)) foreach {case (userType, isAgent) =>
      val form: Form[ReportIncome] = formProvider(userType)
      s"must return OK and the correct view for a GET for the userType $userType" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, reportIncomeRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ReportIncomeView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, userType, NormalMode)(request, messages(application)).toString
        }
      }

      s"must populate the view correctly on a GET when the question has previously been answered for the userType $userType" in {

        val userAnswers = UserAnswers(userAnswersId).set(ReportIncomePage, ReportIncome.values.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, reportIncomeRoute)

          val view = application.injector.instanceOf[ReportIncomeView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(ReportIncome.values.head), taxYear, userType, NormalMode)(request, messages(application)).toString
        }
      }

      s"must return a Bad Request and errors when invalid data is submitted for the userType $userType" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, reportIncomeRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[ReportIncomeView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, userType, NormalMode)(request, messages(application)).toString
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[UkAndForeignPropertyNavigator].toInstance(new FakeUKAndForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reportIncomeRoute)
            .withFormUrlEncodedBody(("value", ReportIncome.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, reportIncomeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, reportIncomeRoute)
            .withFormUrlEncodedBody(("value", ReportIncome.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
