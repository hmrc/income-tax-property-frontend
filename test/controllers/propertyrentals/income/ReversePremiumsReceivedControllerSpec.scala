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

package controllers.propertyrentals.income

import base.SpecBase
import forms.propertyrentals.income.ReversePremiumsReceivedFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, ReversePremiumsReceived, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.propertyrentals.income.ReversePremiumsReceivedPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.propertyrentals.income.ReversePremiumsReceivedView

import java.time.LocalDate
import scala.concurrent.Future

class ReversePremiumsReceivedControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/rentals/deducting-tax")
  private val taxYear = LocalDate.now.getYear

  val formProvider = new ReversePremiumsReceivedFormProvider()
  val form = formProvider("individual")

  lazy val rentalsReversePremiumsReceivedRoute =
    routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode, Rentals).url
  lazy val rentalsRentARoomReversePremiumsReceivedRoute =
    routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "ReversePremiumsReceived Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val view = application.injector.instanceOf[ReversePremiumsReceivedView]

        val rentalsRequest = FakeRequest(GET, rentalsReversePremiumsReceivedRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, NormalMode, taxYear, "individual", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomReversePremiumsReceivedRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual OK
        contentAsString(rentalsRentARoomResult) mustEqual view(
          form,
          NormalMode,
          taxYear,
          "individual",
          RentalsRentARoom
        )(rentalsRentARoomRequest, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val rentalsUserAnswers = UserAnswers(userAnswersId)
        .set(ReversePremiumsReceivedPage(Rentals), ReversePremiumsReceived(true, Some(12.34)))
        .success
        .value

      val rentalsRentARoomUserAnswers = UserAnswers(userAnswersId)
        .set(ReversePremiumsReceivedPage(RentalsRentARoom), ReversePremiumsReceived(true, Some(12.34)))
        .success
        .value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), false).build()
      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), false).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsReversePremiumsReceivedRoute)

        val view = rentalsApplication.injector.instanceOf[ReversePremiumsReceivedView]

        val result = route(rentalsApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ReversePremiumsReceived(true, Some(12.34))),
          NormalMode,
          taxYear,
          "individual",
          Rentals
        )(request, messages(rentalsApplication)).toString
      }

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomReversePremiumsReceivedRoute)

        val view = rentalsRentARoomApplication.injector.instanceOf[ReversePremiumsReceivedView]

        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ReversePremiumsReceived(true, Some(12.34))),
          NormalMode,
          taxYear,
          "individual",
          RentalsRentARoom
        )(request, messages(rentalsRentARoomApplication)).toString
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
        val rentalsRequest =
          FakeRequest(POST, rentalsReversePremiumsReceivedRoute)
            .withFormUrlEncodedBody("reversePremiumsReceived" -> "true", "reversePremiums" -> "1234")

        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomReversePremiumsReceivedRoute)
            .withFormUrlEncodedBody("reversePremiumsReceived" -> "true", "reversePremiums" -> "1234")

        val rentalsResult = route(application, rentalsRequest).value
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual onwardRoute.url

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val rentalsRequest =
          FakeRequest(POST, rentalsReversePremiumsReceivedRoute)
            .withFormUrlEncodedBody(("value", ""))

        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomReversePremiumsReceivedRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReversePremiumsReceivedView]

        val rentalsResult = route(application, rentalsRequest).value
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(boundForm, NormalMode, taxYear, "individual", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        status(rentalsRentARoomResult) mustEqual BAD_REQUEST
        contentAsString(rentalsRentARoomResult) mustEqual view(
          boundForm,
          NormalMode,
          taxYear,
          "individual",
          RentalsRentARoom
        )(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val rentalsRequest = FakeRequest(GET, rentalsReversePremiumsReceivedRoute)
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomReversePremiumsReceivedRoute)

        val rentalsResult = route(application, rentalsRequest).value
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

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
          FakeRequest(POST, rentalsReversePremiumsReceivedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomReversePremiumsReceivedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val rentalsResult = route(application, rentalsRequest).value
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }
  }
}
