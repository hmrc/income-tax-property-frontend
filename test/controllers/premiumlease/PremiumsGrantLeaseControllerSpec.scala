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

package controllers.premiumlease

import base.SpecBase
import forms.premiumlease.PremiumsGrantLeaseFormProvider
import models.{NormalMode, PremiumsGrantLease, Rentals, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.premiumlease.{PremiumsGrantLeasePage, ReceivedGrantLeaseAmountPage, YearLeaseAmountPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.premiumlease.PremiumsGrantLeaseView

import java.time.LocalDate
import scala.concurrent.Future

class PremiumsGrantLeaseControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new PremiumsGrantLeaseFormProvider()
  val form = formProvider("agent")
  private val taxYear = LocalDate.now.getYear

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = BigDecimal(50)

  lazy val premiumsGrantLeaseRoute = routes.PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode).url

  "PremiumsGrantLease Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value
        .set(YearLeaseAmountPage(Rentals), 10)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PremiumsGrantLeaseView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, 10, BigDecimal(100), NormalMode, "agent")(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value
        .set(YearLeaseAmountPage(Rentals), 10)
        .success
        .value
        .set(PremiumsGrantLeasePage(Rentals), PremiumsGrantLease(premiumsGrantLeaseYesOrNo = true, Some(validAnswer)))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseRoute)

        val view = application.injector.instanceOf[PremiumsGrantLeaseView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(PremiumsGrantLease(premiumsGrantLeaseYesOrNo = true, Some(validAnswer))),
          taxYear,
          10,
          BigDecimal(100),
          NormalMode,
          "agent"
        )(request, messages(application)).toString
      }
    }

    "must redirect to received grant amount page, when no amount is found in user data for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(YearLeaseAmountPage(Rentals), 10)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.ReceivedGrantLeaseAmountController
          .onPageLoad(taxYear, NormalMode)
          .url
      }
    }

    "must redirect to year Lease amount page, when no period is found in user data for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.YearLeaseAmountController
          .onPageLoad(taxYear, NormalMode, Rentals)
          .url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), value = BigDecimal(10))
        .success
        .value
        .set(YearLeaseAmountPage(Rentals), 3)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(
              ("premiumsGrantLeaseYesOrNo", "false"),
              ("premiumsGrantLeaseAmount", validAnswer.toString())
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to received grant amount page, when no amount is found in user data when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userAnswersId)
        .set(YearLeaseAmountPage(Rentals), 3)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.ReceivedGrantLeaseAmountController
          .onPageLoad(taxYear, NormalMode)
          .url
      }
    }

    "must redirect to year lease amount page, when no amount is found in user data when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.YearLeaseAmountController
          .onPageLoad(taxYear, NormalMode, Rentals)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value
        .set(YearLeaseAmountPage(Rentals), 10)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), true).build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PremiumsGrantLeaseView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, 10, BigDecimal(100), NormalMode, "agent")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, premiumsGrantLeaseRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, premiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
