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

package controllers.structurebuildingallowance

import base.SpecBase
import controllers.structuresbuildingallowance.routes
import forms.structurebuildingallowance.SbaRemoveConfirmationFormProvider
import models.{NormalMode, Rentals}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.structurebuildingallowance.SbaRemoveConfirmationView

import scala.concurrent.Future
class SbaRemoveConfirmationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new SbaRemoveConfirmationFormProvider()
  private val agent = "agent"
  val form: Form[Boolean] = formProvider(agent)
  val taxYear = 2024
  val index = 1

  lazy val sbaRemoveConfirmationRoute: String =
    routes.SbaRemoveConfirmationController.onPageLoad(taxYear, index, Rentals).url

  "SbaRemoveConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, sbaRemoveConfirmationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SbaRemoveConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, index, NormalMode, "£0", Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, sbaRemoveConfirmationRoute)
            .withFormUrlEncodedBody(("sbaRemoveConfirmation", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, sbaRemoveConfirmationRoute)
            .withFormUrlEncodedBody(("sbaRemoveConfirmation", ""))

        val boundForm = form.bind(Map("sbaRemoveConfirmation" -> ""))

        val view = application.injector.instanceOf[SbaRemoveConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, index, NormalMode, "£0", Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, sbaRemoveConfirmationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, sbaRemoveConfirmationRoute)
            .withFormUrlEncodedBody(("sbaRemoveConfirmation", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
