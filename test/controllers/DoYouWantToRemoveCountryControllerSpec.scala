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

package controllers

import base.SpecBase
import forms.DoYouWantToRemoveCountryFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.{Country, DoYouWantToRemoveCountryPage, SelectIncomeCountryPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.DoYouWantToRemoveCountryView

import scala.concurrent.Future

class DoYouWantToRemoveCountryControllerSpec extends SpecBase with MockitoSugar {
  val taxYear = 2024
  def onwardRoute: Call = Call("GET", "foo")
  val country: Country = Country("Spain", "ESP")
  val formProvider = new DoYouWantToRemoveCountryFormProvider()
  val form: Form[Boolean] = formProvider()
  val index = 0
  lazy val doYouWantToRemoveCountryRoute: String =
    controllers.foreign.routes.DoYouWantToRemoveCountryController.onPageLoad(taxYear, index, NormalMode).url
  val isAgent = false

  "DoYouWantToRemoveCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(
        userAnswers = emptyUserAnswers.set(SelectIncomeCountryPage(index), country).toOption,
        isAgent
      ).build()

      running(application) {
        val request = FakeRequest(GET, doYouWantToRemoveCountryRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DoYouWantToRemoveCountryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, index, NormalMode, country.name)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswersUpdated = for {
        withYesNo          <- UserAnswers(userAnswersId).set(DoYouWantToRemoveCountryPage, true)
        updatedUserAnswers <- withYesNo.set(SelectIncomeCountryPage(index), country)
      } yield updatedUserAnswers

      val application =
        applicationBuilder(userAnswers = userAnswersUpdated.toOption, isAgent)
          .build()

      running(application) {
        val request = FakeRequest(GET, doYouWantToRemoveCountryRoute)

        val view = application.injector.instanceOf[DoYouWantToRemoveCountryView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, taxYear, index, NormalMode, country.name)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(
          userAnswers = emptyUserAnswers.set(SelectIncomeCountryPage(index), country).toOption,
          isAgent
        )
          .overrides(
            bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, doYouWantToRemoveCountryRoute)
            .withFormUrlEncodedBody(("doYouWantToRemoveCountryYesOrNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(
          userAnswers = emptyUserAnswers.set(SelectIncomeCountryPage(index), country).toOption,
          isAgent
        )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, doYouWantToRemoveCountryRoute)
            .withFormUrlEncodedBody(("doYouWantToRemoveCountryYesOrNo", ""))

        val boundForm = form.bind(Map("doYouWantToRemoveCountryYesOrNo" -> ""))

        val view = application.injector.instanceOf[DoYouWantToRemoveCountryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, index, NormalMode, country.name)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, doYouWantToRemoveCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, doYouWantToRemoveCountryRoute)
            .withFormUrlEncodedBody(("doYouWantToRemoveCountryYesOrNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
