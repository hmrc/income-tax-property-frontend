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

package controllers.ukrentaroom.expenses

import base.SpecBase
import controllers.ukrentaroom.expenses.routes.ConsolidatedExpensesRRController
import forms.ConsolidatedRRExpensesFormProvider
import models.{ConsolidatedRRExpenses, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.expenses.ConsolidatedExpensesRRPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.expenses.ConsolidatedExpensesRRView

import java.time.LocalDate
import scala.concurrent.Future

class ConsolidatedExpensesRRControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/")
  private val taxYear = LocalDate.now.getYear

  val formProvider = new ConsolidatedRRExpensesFormProvider()
  val form = formProvider("individual")

  lazy val consolidatedRRExpensesRoute = ConsolidatedExpensesRRController.onPageLoad(taxYear, NormalMode).url

  "ConsolidatedRRExpenses Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, consolidatedRRExpensesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConsolidatedExpensesRRView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, taxYear, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ConsolidatedExpensesRRPage, ConsolidatedRRExpenses(true, Some(12.34)))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, consolidatedRRExpensesRoute)

        val view = application.injector.instanceOf[ConsolidatedExpensesRRView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ConsolidatedRRExpenses(true, Some(12.34))),
          NormalMode,
          taxYear,
          "individual"
        )(request, messages(application)).toString
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
          FakeRequest(POST, consolidatedRRExpensesRoute)
            .withFormUrlEncodedBody("consolidatedExpensesYesOrNo" -> "true", "consolidatedExpensesAmount" -> "1234")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, consolidatedRRExpensesRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ConsolidatedExpensesRRView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, consolidatedRRExpensesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, consolidatedRRExpensesRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
