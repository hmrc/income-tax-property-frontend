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

package controllers.foreign.income

import base.SpecBase
import controllers.routes
import forms.foreign.income.ForeignIncomeSectionCompleteFormProvider
import models.JourneyPath.ForeignPropertyIncome
import models.{JourneyContext, User, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.income.ForeignIncomeSectionCompletePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.JourneyAnswersService
import views.html.foreign.income.ForeignIncomeSectionCompleteView

import scala.concurrent.Future

class ForeignIncomeCompleteControllerSpec extends SpecBase with MockitoSugar {

  val taxYear = 2024
  val countryCode = "AUS"
  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ForeignIncomeSectionCompleteFormProvider()
  val form: Form[Boolean] = formProvider()

  val user: User =
    User(
      mtditid = "mtditid",
      nino = "nino",
      affinityGroup = "affinityGroup",
      agentRef = None
    )

  lazy val foreignIncomeSectionCompleteRoute: String =
    controllers.foreign.income.routes.ForeignIncomeCompleteController.onPageLoad(taxYear, countryCode).url

  "ForeignIncomeSectionComplete Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignIncomeSectionCompleteRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignIncomeSectionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val mockJourneyAnswersService = mock[JourneyAnswersService]
      when(
        mockJourneyAnswersService.setForeignStatus(
          ArgumentMatchers.eq(
            JourneyContext(taxYear, mtditid = "mtditid", nino = "nino", journeyPath = ForeignPropertyIncome)
          ),
          ArgumentMatchers.eq("completed"),
          ArgumentMatchers.eq(user),
          ArgumentMatchers.eq(countryCode)
        )(any())
      ) thenReturn Future.successful(
        Right("")
      )

      val userAnswers =
        UserAnswers(userAnswersId).set(ForeignIncomeSectionCompletePage(countryCode), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignIncomeSectionCompleteRoute)

        val view = application.injector.instanceOf[ForeignIncomeSectionCompleteView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), taxYear, countryCode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the summary page when the user selects Yes to confirm that the journey is finished" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockJourneyAnswersService = mock[JourneyAnswersService]
      when(
        mockJourneyAnswersService.setForeignStatus(
          ArgumentMatchers.eq(
            JourneyContext(taxYear, mtditid = "mtditid", nino = "nino", journeyPath = ForeignPropertyIncome)
          ),
          ArgumentMatchers.eq("completed"),
          ArgumentMatchers.eq(user),
          ArgumentMatchers.eq(countryCode)
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
          FakeRequest(POST, foreignIncomeSectionCompleteRoute)
            .withFormUrlEncodedBody(("isForeignIncomeSectionComplete", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignIncomeSectionCompleteRoute)
            .withFormUrlEncodedBody(("isForeignIncomeSectionComplete", ""))

        val boundForm = form.bind(Map("isForeignIncomeSectionComplete" -> ""))

        val view = application.injector.instanceOf[ForeignIncomeSectionCompleteView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, foreignIncomeSectionCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignIncomeSectionCompleteRoute)
            .withFormUrlEncodedBody(("isForeignIncomeSectionComplete", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
