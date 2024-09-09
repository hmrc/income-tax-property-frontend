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

package controllers.adjustments

import base.SpecBase
import forms.adjustments.UnusedResidentialFinanceCostFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustments.UnusedResidentialFinanceCostPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.adjustments.UnusedResidentialFinanceCostView

import scala.concurrent.Future

class UnusedResidentialCostControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new UnusedResidentialFinanceCostFormProvider()

  val form: Form[BigDecimal] = formProvider("individual")
  val taxYear = 2023
  val validAnswer: BigDecimal = BigDecimal(100.65)

  def onwardRoute: Call = Call("GET", "/unused-residential-finance-cost")

  lazy val rentalsRoute: String = routes.UnusedResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, Rentals).url
  lazy val rentalsRentARoomRoute: String = routes.UnusedResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "UnusedResidentialFinanceCost Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnusedResidentialFinanceCostView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, Rentals)(request, messages(application)).toString
      }
    }

    "Rentals only journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(UnusedResidentialFinanceCostPage(Rentals), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRoute)

        val view = application.injector.instanceOf[UnusedResidentialFinanceCostView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, NormalMode, Rentals)(request, messages(application)).toString
      }
    }

    "Rentals RaR journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(UnusedResidentialFinanceCostPage(RentalsRentARoom), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRentARoomRoute)

        val view = application.injector.instanceOf[UnusedResidentialFinanceCostView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, NormalMode, RentalsRentARoom)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRoute)
            .withFormUrlEncodedBody(("unusedResidentialFinanceCost", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UnusedResidentialFinanceCostView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, Rentals)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
