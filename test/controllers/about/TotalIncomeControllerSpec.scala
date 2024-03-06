/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.about

import base.SpecBase
import forms.about.TotalIncomeFormProvider
import models.{NormalMode, TotalIncome, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.TotalIncomePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.about.TotalIncomeView

import java.time.LocalDate
import scala.concurrent.Future

class TotalIncomeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/update-and-submit-income-tax-return/property/2023/summary")

  val taxYear = LocalDate.now.getYear
  lazy val totalIncomeRoute = routes.TotalIncomeController.onPageLoad(taxYear, NormalMode).url

  val formProvider = new TotalIncomeFormProvider()
  val form = formProvider()

  "totalIncome Controller" - {

    "must return OK and the correct view for a GET when an individual" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, totalIncomeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TotalIncomeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, totalIncomeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TotalIncomeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "agent")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(TotalIncomePage, TotalIncome.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, totalIncomeRoute)

        val view = application.injector.instanceOf[TotalIncomeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TotalIncome.values.head), taxYear, NormalMode, "agent")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, totalIncomeRoute)
            .withFormUrlEncodedBody(("value", TotalIncome.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), true).build()

      running(application) {
        val request =
          FakeRequest(POST, totalIncomeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TotalIncomeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "agent")(request, messages(application)).toString
      }
    }
  }
}
