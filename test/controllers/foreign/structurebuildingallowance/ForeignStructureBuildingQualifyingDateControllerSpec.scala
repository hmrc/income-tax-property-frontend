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
import controllers.structuresbuildingallowance.routes
import forms.foreign.structurebuildingallowance.ForeignStructureBuildingQualifyingDateFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.structurebuildingallowance.ForeignStructureBuildingQualifyingDatePage
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.structurebuildingallowance.ForeignStructureBuildingQualifyingDateView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class ForeignStructureBuildingQualifyingDateControllerSpec extends SpecBase with MockitoSugar {

  lazy val requestRoute: String =
    controllers.foreign.structuresbuildingallowance.routes.ForeignStructureBuildingQualifyingDateController.onPageLoad(taxYear, countryCode, index, NormalMode).url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)
  val formProvider = new ForeignStructureBuildingQualifyingDateFormProvider()
  val taxYear = 2024
  val countryCode = "AUS"
  val index = 0
  val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)
  private val isAgentMessageKey = "individual"

  def onwardRoute: Call = Call("GET", "/foo")

  def getRequest(requestRoute: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, requestRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, requestRoute)
      .withFormUrlEncodedBody(
        "foreignStructureBuildingQualifyingDate.day"   -> validAnswer.getDayOfMonth.toString,
        "foreignStructureBuildingQualifyingDate.month" -> validAnswer.getMonthValue.toString,
        "foreignStructureBuildingQualifyingDate.year"  -> validAnswer.getYear.toString
      )

  private def form = formProvider()

  "ForeignStructureBuildingQualifyingDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val result = route(application, getRequest(requestRoute)).value

        val view = application.injector.instanceOf[ForeignStructureBuildingQualifyingDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, countryCode, index, isAgentMessageKey, NormalMode)(
          getRequest(requestRoute),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(ForeignStructureBuildingQualifyingDatePage(countryCode, index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val view = application.injector.instanceOf[ForeignStructureBuildingQualifyingDateView]

        val result = route(application, getRequest(requestRoute)).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(validAnswer), taxYear, countryCode, index, isAgentMessageKey, NormalMode)(
            getRequest(requestRoute),
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
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      val request =
        FakeRequest(POST, requestRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ForeignStructureBuildingQualifyingDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, countryCode, index, isAgentMessageKey, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val result = route(application, getRequest(requestRoute)).value

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
