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
import forms.structurebuildingallowance.StructureBuildingAllowanceClaimFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.structurebuildingallowance.StructureBuildingAllowanceClaimPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.structurebuildingallowance.StructureBuildingAllowanceClaimView

import scala.concurrent.Future

class StructureBuildingAllowanceClaimControllerSpec extends SpecBase with MockitoSugar {

  lazy val rentalsStructureBuildingAllowanceClaimRoute: String =
    routes.StructureBuildingAllowanceClaimController.onPageLoad(taxYear, NormalMode, index, Rentals).url
  lazy val rentalsRentARoomStructureBuildingAllowanceClaimRoute: String =
    routes.StructureBuildingAllowanceClaimController.onPageLoad(taxYear, NormalMode, index, RentalsRentARoom).url

  val formProvider = new StructureBuildingAllowanceClaimFormProvider()
  val form: Form[BigDecimal] = formProvider(isAgentMessageKey)
  val validAnswer: BigDecimal = BigDecimal(0)
  val taxYear = 2023
  val index = 0
  private val isAgentMessageKey = "individual"

  def onwardRoute: Call = Call("GET", "/foo")

  "StructureBuildingAllowanceClaim Controller" - {

    "must return OK and the correct view for a GET for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      val view = application.injector.instanceOf[StructureBuildingAllowanceClaimView]

      running(application) {
        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsStructureBuildingAllowanceClaimRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, taxYear, isAgentMessageKey, NormalMode, index, Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rent a Room
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomStructureBuildingAllowanceClaimRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual OK
        contentAsString(rentalsRentARoomResult) mustEqual view(
          form,
          taxYear,
          isAgentMessageKey,
          NormalMode,
          index,
          RentalsRentARoom
        )(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for both the Rentals and Rentals and Rent a Room journeys" in {

      val rentalsUserAnswers =
        UserAnswers(userAnswersId).set(StructureBuildingAllowanceClaimPage(index, Rentals), validAnswer).success.value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), isAgent = false).build()

      running(rentalsApplication) {
        val request = FakeRequest(GET, rentalsStructureBuildingAllowanceClaimRoute)
        val result = route(rentalsApplication, request).value
        val view = rentalsApplication.injector.instanceOf[StructureBuildingAllowanceClaimView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(validAnswer),
          taxYear,
          isAgentMessageKey,
          NormalMode,
          index,
          Rentals
        )(
          request,
          messages(rentalsApplication)
        ).toString
      }

      // Rentals and Rent a Room
      val rentalsRentARoomUserAnswers =
        UserAnswers(userAnswersId)
          .set(StructureBuildingAllowanceClaimPage(index, RentalsRentARoom), validAnswer)
          .success
          .value

      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), isAgent = false).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, rentalsRentARoomStructureBuildingAllowanceClaimRoute)
        val result = route(rentalsRentARoomApplication, request).value
        val view = rentalsRentARoomApplication.injector.instanceOf[StructureBuildingAllowanceClaimView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(validAnswer),
          taxYear,
          isAgentMessageKey,
          NormalMode,
          index,
          RentalsRentARoom
        )(
          request,
          messages(rentalsRentARoomApplication)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted for both the Rentals and Rentals and Rent a Room journeys" in {

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
        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsStructureBuildingAllowanceClaimRoute)
            .withFormUrlEncodedBody(("structureBuildingAllowanceClaim", validAnswer.toString))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual onwardRoute.url

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomStructureBuildingAllowanceClaimRoute)
            .withFormUrlEncodedBody(("structureBuildingAllowanceClaim", validAnswer.toString))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val boundForm = form.bind(Map("structureBuildingAllowanceClaim" -> "invalid value"))
      val view = application.injector.instanceOf[StructureBuildingAllowanceClaimView]

      running(application) {
        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsStructureBuildingAllowanceClaimRoute)
            .withFormUrlEncodedBody(("structureBuildingAllowanceClaim", "invalid value"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(
          boundForm,
          taxYear,
          isAgentMessageKey,
          NormalMode,
          index,
          Rentals
        )(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomStructureBuildingAllowanceClaimRoute)
            .withFormUrlEncodedBody(("structureBuildingAllowanceClaim", "invalid value"))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual BAD_REQUEST
        contentAsString(rentalsRentARoomResult) mustEqual view(
          boundForm,
          taxYear,
          isAgentMessageKey,
          NormalMode,
          index,
          RentalsRentARoom
        )(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsStructureBuildingAllowanceClaimRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        // Rentals and Rent a Room
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomStructureBuildingAllowanceClaimRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsStructureBuildingAllowanceClaimRoute)
            .withFormUrlEncodedBody(("structureBuildingAllowanceClaim", validAnswer.toString))

        val rentalsResult = route(application, rentalsRequest).value
        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomStructureBuildingAllowanceClaimRoute)
            .withFormUrlEncodedBody(("structureBuildingAllowanceClaim", validAnswer.toString))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value
        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }
  }
}
