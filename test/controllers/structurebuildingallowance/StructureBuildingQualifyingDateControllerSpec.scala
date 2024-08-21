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
import forms.structurebuildingallowance.StructureBuildingQualifyingDateFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.structurebuildingallowance.StructureBuildingQualifyingDatePage
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.structurebuildingallowance.StructureBuildingQualifyingDateView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class StructureBuildingQualifyingDateControllerSpec extends SpecBase with MockitoSugar {

  lazy val rentalsRoute: String =
    routes.StructureBuildingQualifyingDateController.onPageLoad(taxYear, NormalMode, index, Rentals).url
  lazy val rentalsRaRRoute: String =
    routes.StructureBuildingQualifyingDateController.onPageLoad(taxYear, NormalMode, index, RentalsRentARoom).url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)
  val formProvider = new StructureBuildingQualifyingDateFormProvider()
  val taxYear = 2024
  val index = 0
  val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)
  private val isAgentMessageKey = "individual"

  def onwardRoute: Call = Call("GET", "/foo")

  def getRequest(route: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, route)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, rentalsRoute)
      .withFormUrlEncodedBody(
        "structureBuildingQualifyingDate.day"   -> validAnswer.getDayOfMonth.toString,
        "structureBuildingQualifyingDate.month" -> validAnswer.getMonthValue.toString,
        "structureBuildingQualifyingDate.year"  -> validAnswer.getYear.toString
      )

  private def form = formProvider()

  "StructureBuildingQualifyingDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val result = route(application, getRequest(rentalsRoute)).value

        val view = application.injector.instanceOf[StructureBuildingQualifyingDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, isAgentMessageKey, NormalMode, index, Rentals)(
          getRequest(rentalsRoute),
          messages(application)
        ).toString
      }
    }

    "Rentals journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(StructureBuildingQualifyingDatePage(index, Rentals), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val view = application.injector.instanceOf[StructureBuildingQualifyingDateView]

        val result = route(application, getRequest(rentalsRoute)).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(validAnswer), taxYear, isAgentMessageKey, NormalMode, index, Rentals)(
            getRequest(rentalsRoute),
            messages(application)
          ).toString
      }
    }

    "Rentals and RaR journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(StructureBuildingQualifyingDatePage(index, RentalsRentARoom), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val view = application.injector.instanceOf[StructureBuildingQualifyingDateView]

        val result = route(application, getRequest(rentalsRaRRoute)).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(validAnswer), taxYear, isAgentMessageKey, NormalMode, index, RentalsRentARoom)(
            getRequest(rentalsRaRRoute),
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
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      val request =
        FakeRequest(POST, rentalsRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[StructureBuildingQualifyingDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageKey, NormalMode, index, Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val result = route(application, getRequest(rentalsRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
