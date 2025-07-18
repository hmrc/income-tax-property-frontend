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

package controllers.rentalsandrentaroom.income

import base.SpecBase
import connectors.JourneyAnswersConnector
import forms.rentalsandrentaroom.income.RentalsAndRaRIncomeCompleteFormProvider
import models.JourneyPath.PropertyRentalsAndRentARoomIncome
import models.{JourneyContext, NormalMode, User, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doReturn, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.rentalsandrentaroom.income.RentalsRaRIncomeCompletePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.JourneyAnswersService
import testHelpers.UserHelper.aUser
import uk.gov.hmrc.http.HeaderCarrier
import views.html.rentalsandrentaroom.income.RentalsRaRIncomeCompleteView

import scala.concurrent.Future

class RentalsRaRIncomeCompleteControllerSpec extends SpecBase with MockitoSugar {

  private def postOnwardRoute =
    Call("POST", "/income-tax-property/2024/rentals-rent-a-room/income/complete-yes-no ")

  val formProvider = new RentalsAndRaRIncomeCompleteFormProvider()
  val form = formProvider()
  val taxYear = 2024
  implicit val hc = HeaderCarrier()
  val user = aUser.copy(
    mtditid = "mtditid",
    nino = "nino",
    affinityGroup = "affinityGroup",
    agentRef = None
  )

  lazy val rentalsRaRIncomeCompleteRoute = routes.RentalsRaRIncomeCompleteController.onPageLoad(taxYear).url

  "RentalsRaRIncomeCompleteControllerSpec Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRaRIncomeCompleteRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RentalsRaRIncomeCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(RentalsRaRIncomeCompletePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRaRIncomeCompleteRoute)

        val view = application.injector.instanceOf[RentalsRaRIncomeCompleteView]

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
        .setUKPropertyStatus(
          ArgumentMatchers.eq(
            JourneyContext(
              taxYear = taxYear,
              mtditid = user.mtditid,
              nino = user.nino,
              journeyPath = PropertyRentalsAndRentARoomIncome
            )
          ),
          ArgumentMatchers.eq("completed"),
          ArgumentMatchers.eq(user)
        )(any())

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(postOnwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[JourneyAnswersService].toInstance(mockJourneyAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RentalsRaRIncomeCompleteController.onSubmit(taxYear).url)
            .withFormUrlEncodedBody(("isRentalsRentARoomSectionComplete", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual postOnwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockJourneyAnswersConnector = mock[JourneyAnswersConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(
        mockJourneyAnswersConnector.setStatus(any(), any(), any(), any(), any())(any())
      ).thenReturn(
        Future.successful(
          Right("")
        )
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RentalsRaRIncomeCompleteController.onSubmit(taxYear).url)
            .withFormUrlEncodedBody(("isRentalsRentARoomSectionComplete", ""))

        val boundForm = form.bind(Map("isRentalsRentARoomSectionComplete" -> ""))

        val view = application.injector.instanceOf[RentalsRaRIncomeCompleteView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRaRIncomeCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, false).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRaRIncomeCompleteRoute)
            .withFormUrlEncodedBody(("isRentalsRentARoomSectionComplete", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
