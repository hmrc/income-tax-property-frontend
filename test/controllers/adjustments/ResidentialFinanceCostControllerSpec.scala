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
import forms.adjustments.ResidentialFinanceCostFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustments.ResidentialFinanceCostPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.adjustments.ResidentialFinanceCostView

import scala.concurrent.Future

class ResidentialFinanceCostControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new ResidentialFinanceCostFormProvider()
  val form: Form[BigDecimal] = formProvider("individual")
  val taxYear = 2023
  val validAnswer: BigDecimal = BigDecimal(100.65)

  lazy val rentalsRoute: String = routes.ResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, Rentals).url

  lazy val rentalsRaRRoute: String = routes.ResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "ResidentialFinanceCost Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ResidentialFinanceCostView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, taxYear, "individual", Rentals)(request, messages(application)).toString
      }
    }

    "On rentals only journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ResidentialFinanceCostPage(Rentals), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRoute)

        val view = application.injector.instanceOf[ResidentialFinanceCostView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, taxYear, "individual", Rentals)(request, messages(application)).toString
      }
    }

    "On rentals RaR journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ResidentialFinanceCostPage(RentalsRentARoom), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRaRRoute)

        val view = application.injector.instanceOf[ResidentialFinanceCostView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, taxYear, "individual", RentalsRentARoom)(request, messages(application)).toString
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
            .withFormUrlEncodedBody(("residentialFinanceCost", validAnswer.toString))

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

        val view = application.injector.instanceOf[ResidentialFinanceCostView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, "individual", Rentals)(request, messages(application)).toString
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
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
