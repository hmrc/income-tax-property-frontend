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
import forms.ukrentaroom.JointlyLetFormProvider
import models.requests.DataRequest
import models.{NormalMode, RentARoom, RentalsRentARoom, User, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.ukrentaroom.JointlyLetPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.JointlyLetView

import scala.concurrent.Future

class JointlyLetControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new JointlyLetFormProvider()

  val taxYear = 2024

  lazy val rentARoomJointlyLetRoute =
    controllers.ukrentaroom.routes.JointlyLetController.onPageLoad(taxYear, NormalMode, RentARoom).url

  lazy val rentalsArndRentARoomJointlyLetRoute =
    controllers.ukrentaroom.routes.JointlyLetController
      .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
      .url

  val scenarios = Table[Boolean, String](("Is Agent", "AgencyOrIndividual"), (true, "agent"), (false, "individual"))

  forAll(scenarios) { (isAgent: Boolean, agencyOrIndividual: String) =>
    val form = formProvider(agencyOrIndividual)
    val user = User(
      "",
      "",
      "",
      Option.when(isAgent)("agentReferenceNumber")
    )
    s"JointlyLet Controller for $agencyOrIndividual" - {

      "must return OK and the correct view for a GET for both rent a room and combined journeys" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

        running(application) {
          val rentARoomRequest = FakeRequest(GET, rentARoomJointlyLetRoute)
          val rentARoomResult = route(application, rentARoomRequest).value
          val view = application.injector.instanceOf[JointlyLetView]

          status(rentARoomResult) mustEqual OK
          contentAsString(rentARoomResult) mustEqual view(form, taxYear, NormalMode, RentARoom)(
            DataRequest(rentARoomRequest, "", user, emptyUserAnswers),
            messages(application)
          ).toString

          val rentalsAndRentARoomRequest = FakeRequest(GET, rentalsArndRentARoomJointlyLetRoute)
          val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value

          status(rentalsAndRentARoomResult) mustEqual OK
          contentAsString(rentalsAndRentARoomResult) mustEqual view(form, taxYear, NormalMode, RentalsRentARoom)(
            DataRequest(rentalsAndRentARoomRequest, "", user, emptyUserAnswers),
            messages(application)
          ).toString

        }
      }

      "must populate the view correctly on a GET when the question has previously been answered for both rent a room and combined journeys" in {

        val rentARoomUserAnswers =
          UserAnswers(userAnswersId).set(JointlyLetPage(RentARoom), true).success.value
        val rentARoomJourney = applicationBuilder(userAnswers = Some(rentARoomUserAnswers), isAgent).build()

        running(rentARoomJourney) {
          val rentARoomRequest = FakeRequest(GET, rentARoomJointlyLetRoute)
          val view = rentARoomJourney.injector.instanceOf[JointlyLetView]
          val rentARoomResult = route(rentARoomJourney, rentARoomRequest).value

          status(rentARoomResult) mustEqual OK
          contentAsString(rentARoomResult) mustEqual view(
            form.fill(true),
            taxYear,
            NormalMode,
            RentARoom
          )(
            DataRequest(rentARoomRequest, "", user, emptyUserAnswers),
            messages(rentARoomJourney)
          ).toString
        }

        val rentalsAndRentARoomUserAnswers =
          UserAnswers(userAnswersId).set(JointlyLetPage(RentARoom), true).success.value
        val rentalsAndRentARoomJourney =
          applicationBuilder(userAnswers = Some(rentalsAndRentARoomUserAnswers), isAgent).build()

        running(rentalsAndRentARoomJourney) {

          val rentARoomRequest = FakeRequest(GET, rentARoomJointlyLetRoute)
          val view = rentalsAndRentARoomJourney.injector.instanceOf[JointlyLetView]
          val rentalsAndRentARoomResult = route(rentalsAndRentARoomJourney, rentARoomRequest).value

          status(rentalsAndRentARoomResult) mustEqual OK
          contentAsString(rentalsAndRentARoomResult) mustEqual view(
            form.fill(true),
            taxYear,
            NormalMode,
            RentARoom
          )(
            DataRequest(rentARoomRequest, "", user, emptyUserAnswers),
            messages(rentalsAndRentARoomJourney)
          ).toString
        }
      }

      "must redirect to the next page when valid data is submitted for both rent a room and combined journeys" in {

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
          val rentARoomRequest =
            FakeRequest(POST, rentARoomJointlyLetRoute)
              .withFormUrlEncodedBody(("jointlyLetYesOrNo", "true"))

          val rentARoomResult = route(application, rentARoomRequest).value

          status(rentARoomResult) mustEqual SEE_OTHER
          redirectLocation(rentARoomResult).value mustEqual onwardRoute.url

          val rentalsAndRentARoomRequest =
            FakeRequest(POST, rentalsArndRentARoomJointlyLetRoute)
              .withFormUrlEncodedBody(("jointlyLetYesOrNo", "true"))

          val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value

          status(rentalsAndRentARoomResult) mustEqual SEE_OTHER
          redirectLocation(rentalsAndRentARoomResult).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted for rent a room and combined journeys" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

        running(application) {
          val rentARoomRequest =
            FakeRequest(POST, rentARoomJointlyLetRoute)
              .withFormUrlEncodedBody(("jointlyLetYesOrNo", ""))

          val boundForm = form.bind(Map("jointlyLetYesOrNo" -> ""))
          val view = application.injector.instanceOf[JointlyLetView]

          val rentARoomResult = route(application, rentARoomRequest).value

          status(rentARoomResult) mustEqual BAD_REQUEST
          contentAsString(rentARoomResult) mustEqual view(boundForm, taxYear, NormalMode, RentARoom)(
            DataRequest(rentARoomRequest, "", user, emptyUserAnswers),
            messages(application)
          ).toString

          val rentalsAndRentARoomRequest =
            FakeRequest(POST, rentalsArndRentARoomJointlyLetRoute)
              .withFormUrlEncodedBody(("jointlyLetYesOrNo", ""))

          val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value

          status(rentalsAndRentARoomResult) mustEqual BAD_REQUEST
          contentAsString(rentalsAndRentARoomResult) mustEqual view(
            boundForm,
            taxYear,
            NormalMode,
            RentalsRentARoom
          )(
            DataRequest(rentalsAndRentARoomRequest, "", user, emptyUserAnswers),
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found for both rent a room and combined journeys" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val rentARoomRequest = FakeRequest(GET, rentARoomJointlyLetRoute)

          val rentARoomResult = route(application, rentARoomRequest).value

          status(rentARoomResult) mustEqual SEE_OTHER
          redirectLocation(rentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

          val rentalsAndRentARoomRequest = FakeRequest(GET, rentARoomJointlyLetRoute)

          val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value

          status(rentalsAndRentARoomResult) mustEqual SEE_OTHER
          redirectLocation(rentalsAndRentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found for both rent a room and combined journeys" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val rentARoomRequest =
            FakeRequest(POST, rentARoomJointlyLetRoute)
              .withFormUrlEncodedBody(("jointlyLetYesOrNo", "true"))

          val rentARoomResult = route(application, rentARoomRequest).value

          status(rentARoomResult) mustEqual SEE_OTHER
          redirectLocation(rentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

          val rentalsAndRentARoomRequest =
            FakeRequest(POST, rentalsArndRentARoomJointlyLetRoute)
              .withFormUrlEncodedBody(("jointlyLetYesOrNo", "true"))

          val rentalsAndRentARoomResult = route(application, rentalsAndRentARoomRequest).value

          status(rentalsAndRentARoomResult) mustEqual SEE_OTHER
          redirectLocation(rentalsAndRentARoomResult).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
