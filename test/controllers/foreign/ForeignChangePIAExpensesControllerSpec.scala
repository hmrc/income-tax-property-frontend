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

package controllers.foreign

import base.SpecBase
import controllers.routes
import models.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.ForeignChangePIAExpensesPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.PropertySubmissionService
import views.html.foreign.ForeignChangePIAExpensesView

import scala.concurrent.Future

class ForeignChangePIAExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", controllers.foreign.routes.ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear).url)

  val taxYear = 2024

  lazy val foreignChangePIAExpensesRoute: String = controllers.foreign.routes.ForeignChangePIAExpensesController.onPageLoad(taxYear).url

  "ForeignChangePIAExpenses Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignChangePIAExpensesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignChangePIAExpensesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(propertySubmissionService.deleteForeignPropertyJourneyAnswers(any(), any())(any())).thenReturn(Future.successful(Right(())))
      
      val application =
        applicationBuilder(userAnswers = emptyUserAnswers.set(ForeignChangePIAExpensesPage, true).toOption, isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PropertySubmissionService].toInstance(propertySubmissionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, foreignChangePIAExpensesRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, foreignChangePIAExpensesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignChangePIAExpensesRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
