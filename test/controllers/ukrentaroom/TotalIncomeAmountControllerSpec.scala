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

package controllers.ukrentaroom

import base.SpecBase
import forms.ukrentaroom.TotalIncomeAmountFormProvider
import models.{NormalMode, RentARoom, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.TotalIncomeAmountPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.TotalIncomeAmountView

import scala.concurrent.Future

class TotalIncomeAmountControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new TotalIncomeAmountFormProvider()
  val form = formProvider("individual")

  def onwardRoute: Call = Call("GET", "/foo")

  val totalIncomeAmountAnswer = 100
  val validAnswer = BigDecimal.valueOf(totalIncomeAmountAnswer)

  lazy val rentARoomTotalIncomeAmountRoute: String = routes.TotalIncomeAmountController.onPageLoad(taxYear, NormalMode, RentARoom).url
  lazy val rentalsAndRentARoomIncomeAmountRoute: String = routes.TotalIncomeAmountController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url
  val taxYear: Int = 2023

  "TotalIncomeAmount Controller" - {

    "must return OK and the correct view for a GET Rent a Room journey" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentARoomTotalIncomeAmountRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TotalIncomeAmountView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual", RentARoom)(request, messages(application)).toString
      }
    }
    "must return OK and the correct view for a GET Rentals and Rent a Room journey" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsAndRentARoomIncomeAmountRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TotalIncomeAmountView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual", RentalsRentARoom)(request, messages(application)).toString
      }
    }



    "must populate the Rent a Room journey view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(TotalIncomeAmountPage(RentARoom), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentARoomTotalIncomeAmountRoute)

        val view = application.injector.instanceOf[TotalIncomeAmountView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, NormalMode, "individual", RentARoom)(request, messages(application)).toString
      }
    }

    "must populate the Rentals and Rent a Room journey view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(TotalIncomeAmountPage(RentalsRentARoom), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsAndRentARoomIncomeAmountRoute)

        val view = application.injector.instanceOf[TotalIncomeAmountView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, NormalMode, "individual", RentalsRentARoom)(request, messages(application)).toString
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
        val request =
          FakeRequest(POST, rentARoomTotalIncomeAmountRoute)
            .withFormUrlEncodedBody(("totalIncomeAmount", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted in Rent a Room journey" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, rentARoomTotalIncomeAmountRoute)
            .withFormUrlEncodedBody(("totalIncomeAmount", "invalid value"))

        val boundForm = form.bind(Map("totalIncomeAmount" -> "invalid value"))

        val view = application.injector.instanceOf[TotalIncomeAmountView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "individual", RentARoom)(request, messages(application)).toString
      }
    }
    "must return a Bad Request and errors when invalid data is submitted in Rentals and Rent a Room journey" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsAndRentARoomIncomeAmountRoute)
            .withFormUrlEncodedBody(("totalIncomeAmount", "invalid value"))

        val boundForm = form.bind(Map("totalIncomeAmount" -> "invalid value"))

        val view = application.injector.instanceOf[TotalIncomeAmountView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "individual", RentalsRentARoom)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found in Rent a Room journey" in {

      val application = applicationBuilder(userAnswers = None, false).build()

      running(application) {
        val request = FakeRequest(GET, rentARoomTotalIncomeAmountRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }


    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, false).build()

      running(application) {
        val request =
          FakeRequest(POST, rentARoomTotalIncomeAmountRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
