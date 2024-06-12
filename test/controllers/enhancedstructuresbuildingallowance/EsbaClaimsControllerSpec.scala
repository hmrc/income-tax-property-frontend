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

package controllers.enhancedstructuresbuildingallowance

import base.SpecBase
import forms.enhancedstructuresbuildingallowance.EsbaClaimsFormProvider
import models.EsbasWithSupportingQuestions
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.enhancedstructuresbuildingallowance.Esba
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.PropertySubmissionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist._
import views.html.enhancedstructuresbuildingallowance.EsbaClaimsView

import scala.concurrent.Future

class EsbaClaimsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new EsbaClaimsFormProvider()
  val form: Form[Boolean] = formProvider("agent")

  val taxYear = 2024
  val agent = "agent"
  val list: SummaryList = SummaryListViewModel(Seq.empty)

  lazy val esbaClaimsRoute: String = routes.EsbaClaimsController.onPageLoad(taxYear).url

  "EsbaClaims Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, esbaClaimsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EsbaClaimsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, list, taxYear, agent)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers = emptyUserAnswers.set(EsbasWithSupportingQuestions, EsbasWithSupportingQuestions(true, Some(false), List[Esba]())).get

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockPropertySubmissionService = mock[PropertySubmissionService]
      when(mockPropertySubmissionService.saveJourneyAnswers(any(), any())(any(), any())) thenReturn(Future.successful(Right(())))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PropertySubmissionService].toInstance(mockPropertySubmissionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, esbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, esbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", ""))

        val boundForm = form.bind(Map("anotherClaim" -> ""))

        val view = application.injector.instanceOf[EsbaClaimsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, list, taxYear, agent)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, esbaClaimsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, esbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
