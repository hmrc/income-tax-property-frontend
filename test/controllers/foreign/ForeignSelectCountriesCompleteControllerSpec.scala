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
import controllers.foreign.routes.ForeignSelectCountriesCompleteController
import controllers.routes
import forms.foreign.ForeignSelectCountriesCompleteFormProvider
import models.JourneyPath.ForeignSelectCountry
import models.{JourneyContext, User, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.ForeignSelectCountriesCompletePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.foreign.ForeignSelectCountriesCompleteView

import scala.concurrent.Future

class ForeignSelectCountriesCompleteControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new ForeignSelectCountriesCompleteFormProvider()
  private val form = formProvider()
  val taxYear = 2024
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val user: User =
    User(
      mtditid = "mtditid",
      nino = "nino",
      affinityGroup = "affinityGroup",
      agentRef = None
    )

  private lazy val foreignSelectCountriesCompleteRoute =
    ForeignSelectCountriesCompleteController.onPageLoad(taxYear).url

  private def onwardRoute = Call("GET", "/update-and-submit-income-tax-return/property/2024/summary")

  "ForeignSelectCountriesComplete Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignSelectCountriesCompleteRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignSelectCountriesCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ForeignSelectCountriesCompletePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignSelectCountriesCompleteRoute)

        val view = application.injector.instanceOf[ForeignSelectCountriesCompleteView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to the summary page when the user selects Yes to confirm that the journey is finished" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockJourneyAnswersService = mock[JourneyAnswersService]
      when(
        mockJourneyAnswersService.setForeignStatus(
          ArgumentMatchers.eq(
            JourneyContext(taxYear, mtditid = "mtditid", nino = "nino", journeyPath = ForeignSelectCountry)
          ),
          ArgumentMatchers.eq("completed"),
          ArgumentMatchers.eq(user),
          ArgumentMatchers.eq("")
        )(any())
      ) thenReturn Future.successful(
        Right("")
      )

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
          FakeRequest(POST, foreignSelectCountriesCompleteRoute)
            .withFormUrlEncodedBody(("isForeignSelectCountriesComplete", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignSelectCountriesCompleteRoute)
            .withFormUrlEncodedBody(("isForeignSelectCountriesComplete", ""))

        val boundForm = form.bind(Map("isForeignSelectCountriesComplete" -> ""))

        val view = application.injector.instanceOf[ForeignSelectCountriesCompleteView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignSelectCountriesCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignSelectCountriesCompleteRoute)
            .withFormUrlEncodedBody(("isForeignSelectCountriesComplete", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
