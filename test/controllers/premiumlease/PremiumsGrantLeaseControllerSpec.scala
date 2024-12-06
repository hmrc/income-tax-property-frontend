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
import models.{NormalMode, PremiumsGrantLease, Rentals, RentalsRentARoom, UserAnswers}
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

  lazy val rentalsPremiumsGrantLeaseRoute =
    routes.PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode, Rentals).url

  lazy val rentalsRentARoomPremiumsGrantLeaseRoute =
    routes.PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "PremiumsGrantLease Controller" - {

    "must return OK and the correct view for a GET" in {

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value
        .set(YearLeaseAmountPage(Rentals), 10)
        .success
        .value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), true).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsPremiumsGrantLeaseRoute)

        val result = route(rentalsApplication, request).value

        val view = rentalsApplication.injector.instanceOf[PremiumsGrantLeaseView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, 10, BigDecimal(100), NormalMode, "agent", Rentals)(
          request,
          messages(rentalsApplication)
        ).toString
      }

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(RentalsRentARoom), BigDecimal(100))
        .success
        .value
        .set(YearLeaseAmountPage(RentalsRentARoom), 10)
        .success
        .value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), true).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomPremiumsGrantLeaseRoute)

        val result = route(rentalsRentARoomApplication, request).value

        val view = rentalsRentARoomApplication.injector.instanceOf[PremiumsGrantLeaseView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          taxYear,
          10,
          BigDecimal(100),
          NormalMode,
          "agent",
          RentalsRentARoom
        )(
          request,
          messages(rentalsRentARoomApplication)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value
        .set(YearLeaseAmountPage(Rentals), 10)
        .success
        .value
        .set(PremiumsGrantLeasePage(Rentals), PremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(validAnswer)))
        .success
        .value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), true).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsPremiumsGrantLeaseRoute)

        val view = rentalsApplication.injector.instanceOf[PremiumsGrantLeaseView]

        val result = route(rentalsApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(PremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(validAnswer))),
          taxYear,
          10,
          BigDecimal(100),
          NormalMode,
          "agent",
          Rentals
        )(request, messages(rentalsApplication)).toString
      }

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(RentalsRentARoom), BigDecimal(100))
        .success
        .value
        .set(YearLeaseAmountPage(RentalsRentARoom), 10)
        .success
        .value
        .set(
          PremiumsGrantLeasePage(RentalsRentARoom),
          PremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(validAnswer))
        )
        .success
        .value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), true).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomPremiumsGrantLeaseRoute)

        val view = rentalsRentARoomApplication.injector.instanceOf[PremiumsGrantLeaseView]

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(PremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(validAnswer))),
          taxYear,
          10,
          BigDecimal(100),
          NormalMode,
          "agent",
          RentalsRentARoom
        )(request, messages(rentalsRentARoomApplication)).toString
      }
    }

    "must redirect to received grant amount page, when no reversePremiums is found in user data for a GET" in {

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(YearLeaseAmountPage(Rentals), 10)
        .success
        .value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), true).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsPremiumsGrantLeaseRoute)

        val result = route(rentalsApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.ReceivedGrantLeaseAmountController
          .onPageLoad(taxYear, NormalMode, Rentals)
          .url
      }

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(YearLeaseAmountPage(RentalsRentARoom), 10)
        .success
        .value

      val rentalsRentARoomApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), true).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomPremiumsGrantLeaseRoute)

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.ReceivedGrantLeaseAmountController
          .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
          .url
      }
    }

    "must redirect to year Lease reversePremiums page, when no period is found in user data for a GET" in {

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), true).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsPremiumsGrantLeaseRoute)

        val result = route(rentalsApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.YearLeaseAmountController
          .onPageLoad(taxYear, NormalMode, Rentals)
          .url
      }

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(RentalsRentARoom), BigDecimal(100))
        .success
        .value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), true).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomPremiumsGrantLeaseRoute)

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.YearLeaseAmountController
          .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
          .url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), value = BigDecimal(10))
        .success
        .value
        .set(YearLeaseAmountPage(Rentals), 3)
        .success
        .value

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(RentalsRentARoom), value = BigDecimal(10))
        .success
        .value
        .set(YearLeaseAmountPage(RentalsRentARoom), 3)
        .success
        .value

      val rentalsApplication =
        applicationBuilder(userAnswers = Some(rentalsUserAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(rentalsApplication) {
        val request =
          FakeRequest(POST, rentalsPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(
              ("premiumsGrantLeaseReceived", "false"),
              ("premiumsGrantLeaseAmount", validAnswer.toString())
            )

        val result = route(rentalsApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }

      running(rentalsRentARoomApplication) {
        val request =
          FakeRequest(POST, rentalsRentARoomPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(
              ("premiumsGrantLeaseReceived", "false"),
              ("premiumsGrantLeaseAmount", validAnswer.toString())
            )

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to received grant reversePremiums page, when no reversePremiums is found in user data when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(YearLeaseAmountPage(Rentals), 3)
        .success
        .value

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(YearLeaseAmountPage(RentalsRentARoom), 3)
        .success
        .value

      val rentalsApplication =
        applicationBuilder(userAnswers = Some(rentalsUserAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(rentalsApplication) {
        val request =
          FakeRequest(POST, rentalsPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(rentalsApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.ReceivedGrantLeaseAmountController
          .onPageLoad(taxYear, NormalMode, Rentals)
          .url
      }

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(rentalsRentARoomApplication) {
        val request =
          FakeRequest(POST, rentalsRentARoomPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.ReceivedGrantLeaseAmountController
          .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
          .url
      }
    }

    "must redirect to year lease reversePremiums page, when no reversePremiums is found in user data when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value

      val rentalsApplication =
        applicationBuilder(userAnswers = Some(rentalsUserAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(rentalsApplication) {
        val request =
          FakeRequest(POST, rentalsPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(rentalsApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.YearLeaseAmountController
          .onPageLoad(taxYear, NormalMode, Rentals)
          .url
      }

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(RentalsRentARoom), BigDecimal(100))
        .success
        .value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(rentalsRentARoomApplication) {
        val request =
          FakeRequest(POST, rentalsRentARoomPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.premiumlease.routes.YearLeaseAmountController
          .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(100))
        .success
        .value
        .set(YearLeaseAmountPage(Rentals), 10)
        .success
        .value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), true).build()

      running(rentalsApplication) {
        val request =
          FakeRequest(POST, rentalsPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = rentalsApplication.injector.instanceOf[PremiumsGrantLeaseView]

        val result = route(rentalsApplication, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, 10, BigDecimal(100), NormalMode, "agent", Rentals)(
          request,
          messages(rentalsApplication)
        ).toString
      }

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(ReceivedGrantLeaseAmountPage(RentalsRentARoom), BigDecimal(100))
        .success
        .value
        .set(YearLeaseAmountPage(RentalsRentARoom), 10)
        .success
        .value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), true).build()

      running(rentalsRentARoomApplication) {
        val request =
          FakeRequest(POST, rentalsRentARoomPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = rentalsApplication.injector.instanceOf[PremiumsGrantLeaseView]

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          taxYear,
          10,
          BigDecimal(100),
          NormalMode,
          "agent",
          RentalsRentARoom
        )(
          request,
          messages(rentalsRentARoomApplication)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val rentalsRequest = FakeRequest(GET, rentalsPremiumsGrantLeaseRoute)

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomPremiumsGrantLeaseRoute)

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val rentalsRequest =
          FakeRequest(POST, rentalsPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER

        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomPremiumsGrantLeaseRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER

        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }
  }
}
