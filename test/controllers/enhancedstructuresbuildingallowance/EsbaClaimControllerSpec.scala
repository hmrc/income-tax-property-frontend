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

package controllers.enhancedstructuresbuildingallowance

import base.SpecBase
import controllers.routes
import forms.enhancedstructuresbuildingallowance.EsbaClaimAmountFormProvider
import models.requests.DataRequest
import models.{NormalMode, Rentals, User, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.enhancedstructuresbuildingallowance.EsbaClaimPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.enhancedstructuresbuildingallowance.EsbaClaimView

import scala.concurrent.Future

class EsbaClaimControllerSpec extends SpecBase with MockitoSugar {

  lazy val esbaClaimAmountRoute = controllers.enhancedstructuresbuildingallowance.routes.EsbaClaimController
    .onPageLoad(taxYear, NormalMode, index, Rentals)
    .url
  val formProvider = new EsbaClaimAmountFormProvider()
  val form = formProvider("individual")

  val onwardRoute = Call("GET", "/foo")

  val validAnswer = BigDecimal(0)
  val taxYear = 2024
  val index = 0
  val user = User(
    "",
    "",
    "",
    None
  )
  private val isAgentMessageKey = "individual"
  "EsbaClaimAmount Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = DataRequest(FakeRequest(GET, esbaClaimAmountRoute), "", user, emptyUserAnswers)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EsbaClaimView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, isAgentMessageKey, NormalMode, index, Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(EsbaClaimPage(index, Rentals), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val fakeRequest = FakeRequest(GET, esbaClaimAmountRoute)
        val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

        val view = application.injector.instanceOf[EsbaClaimView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(validAnswer),
          taxYear,
          isAgentMessageKey,
          NormalMode,
          index,
          Rentals
        )(
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
        val fakeRequest =
          FakeRequest(POST, esbaClaimAmountRoute)
            .withFormUrlEncodedBody(("esbaClaim", validAnswer.toString))
        val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val fakeRequest =
          FakeRequest(POST, esbaClaimAmountRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))
        val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EsbaClaimView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageKey, NormalMode, index, Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val fakeRequest = FakeRequest(GET, esbaClaimAmountRoute)
        val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val fakeRequest =
          FakeRequest(POST, esbaClaimAmountRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))
        val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
