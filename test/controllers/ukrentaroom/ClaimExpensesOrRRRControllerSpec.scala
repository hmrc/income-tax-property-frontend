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
import controllers.ukrentaroom.routes
import forms.ukrentaroom.ClaimExpensesOrRRRFormProvider
import models.{ClaimExpensesOrRRR, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.{ClaimExpensesOrRRRPage, UkRentARoomJointlyLetPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.ClaimExpensesOrRRRView

import scala.concurrent.Future

class ClaimExpensesOrRRRControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ClaimExpensesOrRRRFormProvider()
  val form = formProvider("individual")
  val taxYear = 2023

  lazy val claimExpensesOrRRRRoute = routes.ClaimExpensesOrRRRController.onPageLoad(taxYear, NormalMode).url

  "ClaimExpensesOrRRR Controller" - {

    "must return OK and the correct view for a GET" in {
       val answers = emptyUserAnswers.set(UkRentARoomJointlyLetPage, true).get

      val application = applicationBuilder(userAnswers = Some(answers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, claimExpensesOrRRRRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClaimExpensesOrRRRView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual", "jointlyLet")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ClaimExpensesOrRRRPage, ClaimExpensesOrRRR(true, Some(100.65))).success.value
      val completeAnswers = userAnswers.set(UkRentARoomJointlyLetPage, true).get
      val application = applicationBuilder(userAnswers = Some(completeAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, claimExpensesOrRRRRoute)

        val view = application.injector.instanceOf[ClaimExpensesOrRRRView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(ClaimExpensesOrRRR(true, Some(100.65))), taxYear,
          NormalMode, "individual", "jointlyLet")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val answers = emptyUserAnswers.set(UkRentARoomJointlyLetPage, true).get
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(answers), isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, claimExpensesOrRRRRoute)
            .withFormUrlEncodedBody(("claimExpensesOrRRR", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val answers = emptyUserAnswers.set(UkRentARoomJointlyLetPage, true).get
      val application = applicationBuilder(userAnswers = Some(answers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, claimExpensesOrRRRRoute)
            .withFormUrlEncodedBody(("claimExpensesOrRRR", ""))

        val boundForm = form.bind(Map("claimExpensesOrRRR" -> ""))

        val view = application.injector.instanceOf[ClaimExpensesOrRRRView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "individual", "jointlyLet")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, claimExpensesOrRRRRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, claimExpensesOrRRRRoute)
            .withFormUrlEncodedBody(("claimExpensesOrRRR", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}