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

package controllers.rentalsandrentaroom.adjustments

import base.SpecBase
import forms.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompleteFormProvider
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompletePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.JourneyAnswersService
import views.html.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompleteView

import scala.concurrent.Future

class RentalsRaRAdjustmentsCompleteControllerSpec extends SpecBase with MockitoSugar {

  val taxYear = 2024

  def onwardRoute: Call = Call("GET", "/update-and-submit-income-tax-return/property/2024/summary")

  lazy val rentalsRaRAdjustmentsCompleteRoute: String = routes.RentalsRaRAdjustmentsCompleteController.onPageLoad(taxYear).url

  val formProvider = new RentalsRaRAdjustmentsCompleteFormProvider()
  val form: Form[Boolean] = formProvider()

  "RentalsRaRAdjustmentsComplete Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRaRAdjustmentsCompleteRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RentalsRaRAdjustmentsCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(RentalsRaRAdjustmentsCompletePage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRaRAdjustmentsCompleteRoute)

        val view = application.injector.instanceOf[RentalsRaRAdjustmentsCompleteView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), taxYear)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val journeyAnswersService = mock[JourneyAnswersService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(journeyAnswersService.setStatus(any(), any(), any())(any())) thenReturn Future.successful(Right("true"))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[JourneyAnswersService].toInstance(journeyAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRaRAdjustmentsCompleteRoute)
            .withFormUrlEncodedBody(("isRentalsRaRAdjustmentsComplete", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRaRAdjustmentsCompleteRoute)
            .withFormUrlEncodedBody(("isRentalsRaRAdjustmentsComplete", "invalid value"))

        val boundForm = form.bind(Map("isRentalsRaRAdjustmentsComplete" -> "invalid value"))

        val view = application.injector.instanceOf[RentalsRaRAdjustmentsCompleteView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, rentalsRaRAdjustmentsCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, rentalsRaRAdjustmentsCompleteRoute)
            .withFormUrlEncodedBody(("isRentalsRaRAdjustmentsComplete", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
