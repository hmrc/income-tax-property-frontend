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

package controllers.ukrentaroom.adjustments

import base.SpecBase
import forms.ukrentaroom.adjustments.RaRAdjustmentsCompleteFormProvider
import models.JourneyPath.RentARoomAdjustments
import models._
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doReturn, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.adjustments.RaRAdjustmentsCompletePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ukrentaroom.adjustments.RaRAdjustmentsCompleteView

import scala.concurrent.Future

class RaRAdjustmentsCompleteControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new RaRAdjustmentsCompleteFormProvider()
  val form: Form[Boolean] = formProvider()
  val taxYear = 2024
  val user = User(
    mtditid = "mtditid",
    nino = "nino",
    affinityGroup = "affinityGroup",
    agentRef = None
  )
  lazy val raRAdjustmentsCompleteRoute: String =
    controllers.ukrentaroom.adjustments.routes.RaRAdjustmentsCompleteController.onPageLoad(taxYear).url

  implicit val hc: HeaderCarrier = new HeaderCarrier()

  "RaRAdjustmentsComplete Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, raRAdjustmentsCompleteRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RaRAdjustmentsCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(RaRAdjustmentsCompletePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, raRAdjustmentsCompleteRoute)

        val view = application.injector.instanceOf[RaRAdjustmentsCompleteView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), taxYear, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockJourneyAnswersService = mock[JourneyAnswersService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      doReturn(
        Future.successful(
          Right("completed")
        )
      )
        .when(mockJourneyAnswersService)
        .setStatus(
          ArgumentMatchers.eq(
            JourneyContext(
              taxYear = taxYear,
              mtditid = user.mtditid,
              nino = user.nino,
              journeyPath = RentARoomAdjustments
            )
          ),
          ArgumentMatchers.eq("completed"),
          ArgumentMatchers.eq(user)
        )(any())

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[JourneyAnswersService].toInstance(mockJourneyAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, raRAdjustmentsCompleteRoute)
            .withFormUrlEncodedBody(("raRAdjustmentsCompleteYesOrNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, raRAdjustmentsCompleteRoute)
            .withFormUrlEncodedBody(("raRAdjustmentsCompleteYesOrNo", ""))

        val boundForm = form.bind(Map("raRAdjustmentsCompleteYesOrNo" -> ""))

        val view = application.injector.instanceOf[RaRAdjustmentsCompleteView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, raRAdjustmentsCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, raRAdjustmentsCompleteRoute)
            .withFormUrlEncodedBody(("raRAdjustmentsCompleteYesOrNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
