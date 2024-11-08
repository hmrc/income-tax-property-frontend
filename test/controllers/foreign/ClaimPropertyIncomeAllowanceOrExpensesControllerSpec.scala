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
import controllers.routes
import forms.foreign.ClaimPropertyIncomeAllowanceOrExpensesFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.ClaimPropertyIncomeAllowanceOrExpensesPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.ClaimPropertyIncomeAllowanceOrExpensesView

import scala.concurrent.Future

class ClaimPropertyIncomeAllowanceOrExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/update-and-submit-income-tax-return/property/2024/foreign-property/select-country/check-your-answers")

  val formProvider = new ClaimPropertyIncomeAllowanceOrExpensesFormProvider()
  val form = formProvider()
  val taxYear: Int = 2024

  lazy val claimPropertyIncomeAllowanceOrExpensesRoute =
    controllers.foreign.routes.ClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode).url

  "ClaimPropertyIncomeAllowanceOrExpenses Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, claimPropertyIncomeAllowanceOrExpensesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClaimPropertyIncomeAllowanceOrExpensesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, form, NormalMode, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ClaimPropertyIncomeAllowanceOrExpensesPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, claimPropertyIncomeAllowanceOrExpensesRoute)

        val view = application.injector.instanceOf[ClaimPropertyIncomeAllowanceOrExpensesView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, form.fill(true), NormalMode, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, claimPropertyIncomeAllowanceOrExpensesRoute)
            .withFormUrlEncodedBody(("claimPropertyIncomeAllowanceOrExpenses", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, claimPropertyIncomeAllowanceOrExpensesRoute)
            .withFormUrlEncodedBody(("claimPropertyIncomeAllowanceOrExpenses", ""))

        val boundForm = form.bind(Map("claimPropertyIncomeAllowanceOrExpenses" -> ""))

        val view = application.injector.instanceOf[ClaimPropertyIncomeAllowanceOrExpensesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(taxYear, boundForm, NormalMode, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, claimPropertyIncomeAllowanceOrExpensesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, claimPropertyIncomeAllowanceOrExpensesRoute)
            .withFormUrlEncodedBody(("claimPropertyIncomeAllowanceOrExpenses", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
