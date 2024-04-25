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
import controllers.routes
import forms.ukrentaroom.UkRentARoomJointlyLetFormProvider
import models.requests.DataRequest
import models.{NormalMode, User, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.ukrentaroom.UkRentARoomJointlyLetPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.UkRentARoomJointlyLetView

import scala.concurrent.Future

class UkRentARoomJointlyLetControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new UkRentARoomJointlyLetFormProvider()

  val taxYear = 2024

  lazy val rentARoomJointlyLetRoute = controllers.ukrentaroom.routes.UkRentARoomJointlyLetController.onPageLoad(taxYear, NormalMode).url

  val scenarios = Table[Boolean, String](
    ("Is Agent", "AgencyOrIndividual"),
    (true, "agent"),
    (false, "individual"))

  forAll(scenarios) { (isAgent: Boolean, agencyOrIndividual: String) => {
    val form = formProvider(agencyOrIndividual)
    val user = User(
      "",
      "",
      "",
      isAgent
    )
    s"RentARoomJointlyLet Controller for $agencyOrIndividual" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

        running(application) {
          val request = FakeRequest(GET, rentARoomJointlyLetRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UkRentARoomJointlyLetView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, NormalMode)(DataRequest(request, "", user, emptyUserAnswers), messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(UkRentARoomJointlyLetPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent).build()

        running(application) {
          val request = FakeRequest(GET, rentARoomJointlyLetRoute)

          val view = application.injector.instanceOf[UkRentARoomJointlyLetView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), taxYear, NormalMode)(DataRequest(request, "", user, emptyUserAnswers), messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, rentARoomJointlyLetRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, rentARoomJointlyLetRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[UkRentARoomJointlyLetView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode)(DataRequest(request, "", user, emptyUserAnswers), messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val request = FakeRequest(GET, rentARoomJointlyLetRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val request =
            FakeRequest(POST, rentARoomJointlyLetRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
  }

}
