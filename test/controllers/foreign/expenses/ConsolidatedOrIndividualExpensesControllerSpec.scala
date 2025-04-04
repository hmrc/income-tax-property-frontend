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

package controllers.foreign.expenses

import base.SpecBase
import controllers.routes
import controllers.foreign.expenses.routes.ConsolidatedOrIndividualExpensesController
import forms.foreign.expenses.ConsolidatedOrIndividualExpensesFormProvider
import models.{ConsolidatedOrIndividualExpenses, UserAnswers, NormalMode}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.expenses.ConsolidatedOrIndividualExpensesPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.expenses.ConsolidatedOrIndividualExpensesView

import scala.concurrent.Future

class ConsolidatedOrIndividualExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")
  val isAgentMessageKey = "agent"
  val formProvider = new ConsolidatedOrIndividualExpensesFormProvider()
  val form: Form[ConsolidatedOrIndividualExpenses] = formProvider(isAgentMessageKey)
  val taxYear: Int = 2024
  val countryCode: String = "GRC"

  lazy val consolidatedOrIndividualExpensesRoute = ConsolidatedOrIndividualExpensesController.onPageLoad(taxYear, countryCode, NormalMode).url

  "ConsolidatedOrIndividualExpenses Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, consolidatedOrIndividualExpensesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConsolidatedOrIndividualExpensesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, isAgentMessageKey, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val amount = 100
      val formAnswers = ConsolidatedOrIndividualExpenses(
        isConsolidatedOrIndividualExpenses = true,
        Some(amount)
      )
      val userAnswers = UserAnswers(userAnswersId).set(
       ConsolidatedOrIndividualExpensesPage(countryCode),
        formAnswers
      ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, consolidatedOrIndividualExpensesRoute)

        val view = application.injector.instanceOf[ConsolidatedOrIndividualExpensesView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(formAnswers), NormalMode, isAgentMessageKey, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted for yes" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, consolidatedOrIndividualExpensesRoute)
            .withFormUrlEncodedBody(("consolidatedOrIndividualExpenses", "false"), ("consolidatedExpensesAmount", "100"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, consolidatedOrIndividualExpensesRoute)
            .withFormUrlEncodedBody(("isConsolidatedOrIndividualExpenses", "invalid value"))

        val boundForm = form.bind(Map("isConsolidatedOrIndividualExpenses" -> "invalid value"))

        val view = application.injector.instanceOf[ConsolidatedOrIndividualExpensesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, isAgentMessageKey, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, consolidatedOrIndividualExpensesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, consolidatedOrIndividualExpensesRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
