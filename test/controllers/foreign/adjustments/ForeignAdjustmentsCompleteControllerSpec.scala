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

package controllers.foreign.adjustments

import base.SpecBase
import forms.foreign.adjustments.ForeignAdjustmentsCompleteFormProvider
import models.JourneyPath.ForeignPropertyAdjustments
import models.{JourneyContext, NormalMode, User, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doReturn, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.adjustments.ForeignAdjustmentsCompletePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.foreign.adjustments.ForeignAdjustmentsCompleteView

import scala.concurrent.Future

class ForeignAdjustmentsCompleteControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new ForeignAdjustmentsCompleteFormProvider()
  val form: Form[Boolean] = formProvider()
  val taxYear = 2024
  val countryCode: String = "AUS"
  lazy val foreignAdjustmentsCompleteRoute: String =
    controllers.foreign.adjustments.routes.ForeignAdjustmentsCompleteController.onPageLoad(taxYear, countryCode).url

  implicit val hc: HeaderCarrier = new HeaderCarrier()

  "ForeignAdjustmentsComplete Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignAdjustmentsCompleteRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignAdjustmentsCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ForeignAdjustmentsCompletePage(countryCode), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignAdjustmentsCompleteRoute)

        val view = application.injector.instanceOf[ForeignAdjustmentsCompleteView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, taxYear, countryCode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockJourneyAnswersService = mock[JourneyAnswersService]
      val user = User(
        mtditid = "mtditid",
        nino = "nino",
        affinityGroup = "affinityGroup",
        agentRef = None
      )

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      doReturn(
        Future.successful(
          Right("completed")
        )
      )
        .when(mockJourneyAnswersService)
        .setForeignStatus(
          ArgumentMatchers.eq(
            JourneyContext(
              taxYear = taxYear,
              mtditid = user.mtditid,
              nino = user.nino,
              journeyPath = ForeignPropertyAdjustments
            )
          ),
          ArgumentMatchers.eq("completed"),
          ArgumentMatchers.eq(user),
          ArgumentMatchers.eq(countryCode)
        )(any())

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[JourneyAnswersService].toInstance(mockJourneyAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, foreignAdjustmentsCompleteRoute)
            .withFormUrlEncodedBody(("foreignAdjustmentsCompleteYesOrNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignAdjustmentsCompleteRoute)
            .withFormUrlEncodedBody(("foreignAdjustmentsCompleteYesOrNo", ""))

        val boundForm = form.bind(Map("foreignAdjustmentsCompleteYesOrNo" -> ""))

        val view = application.injector.instanceOf[ForeignAdjustmentsCompleteView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, foreignAdjustmentsCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignAdjustmentsCompleteRoute)
            .withFormUrlEncodedBody(("foreignAdjustmentsCompleteYesOrNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
