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
import controllers.routes
import forms.enhancedstructuresbuildingallowance.EsbaSectionFinishedFormProvider
import models.JourneyPath.RentalESBA
import models.{JourneyContext, NormalMode, Rentals, User, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.enhancedstructuresbuildingallowance.EsbaSectionFinishedPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.JourneyAnswersService
import views.html.enhancedstructuresbuildingallowance.EsbaSectionFinishedView

import scala.concurrent.Future

class EsbaSectionFinishedControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new EsbaSectionFinishedFormProvider()
  val form = formProvider()

  val taxYear: Int = 2024
  lazy val esbaSectionFinishedRoute =
    controllers.enhancedstructuresbuildingallowance.routes.EsbaSectionFinishedController
      .onPageLoad(taxYear, Rentals)
      .url

  "EsbaSectionFinished Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, esbaSectionFinishedRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EsbaSectionFinishedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, taxYear, Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(EsbaSectionFinishedPage(Rentals), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, esbaSectionFinishedRoute)

        val view = application.injector.instanceOf[EsbaSectionFinishedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, taxYear, Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockJourneyAnswersService = mock[JourneyAnswersService]
      val user: User = User(
        mtditid = "mtditid",
        nino = "nino",
        affinityGroup = "affinityGroup",
        agentRef = None
      )

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(
        mockJourneyAnswersService.setStatus(
          ArgumentMatchers.eq(
            JourneyContext(taxYear, mtditid = "mtditid", nino = "nino", journeyPath = RentalESBA)
          ),
          ArgumentMatchers.eq("completed"),
          ArgumentMatchers.eq(user)
        )(any())
      ) thenReturn Future.successful(Right(""))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[JourneyAnswersService].toInstance(mockJourneyAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, esbaSectionFinishedRoute)
            .withFormUrlEncodedBody(("esbaSectionFinishedYesOrNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, esbaSectionFinishedRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EsbaSectionFinishedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, esbaSectionFinishedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, esbaSectionFinishedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
