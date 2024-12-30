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

package controllers.foreign.structurebuildingallowance

import base.SpecBase
import controllers.foreign.structuresbuildingallowance.routes.ForeignSbaRemoveConfirmationController
import controllers.routes
import forms.foreign.structurebuildingallowance.ForeignSbaRemoveConfirmationFormProvider
import models.NormalMode
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.structurebuildingallowance.ForeignSbaRemoveConfirmationView

import scala.concurrent.Future

class ForeignSbaRemoveConfirmationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ForeignSbaRemoveConfirmationFormProvider()
  val form = formProvider()
  val countryCode: String = "AUS"
  val taxYear: Int = 2023
  val index: Int = 0
  val list: SummaryList = SummaryListViewModel(Seq.empty)

  lazy val foreignSbaRemoveConfirmationRoute =
    ForeignSbaRemoveConfirmationController.onPageLoad(taxYear, index, countryCode).url

  "ForeignSbaRemoveConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignSbaRemoveConfirmationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignSbaRemoveConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, list, taxYear, index, countryCode, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, foreignSbaRemoveConfirmationRoute)
            .withFormUrlEncodedBody(("foreignSbaRemoveConfirmation", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignSbaRemoveConfirmationRoute)
            .withFormUrlEncodedBody(("foreignSbaRemoveConfirmation", ""))

        val boundForm = form.bind(Map("foreignSbaRemoveConfirmation" -> ""))

        val view = application.injector.instanceOf[ForeignSbaRemoveConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, list, taxYear, index, countryCode, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, foreignSbaRemoveConfirmationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignSbaRemoveConfirmationRoute)
            .withFormUrlEncodedBody(("foreignSbaRemoveConfirmation", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
