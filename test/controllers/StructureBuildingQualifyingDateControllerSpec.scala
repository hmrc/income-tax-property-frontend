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

import java.time.{LocalDate, ZoneOffset}

import base.SpecBase
import forms.StructureBuildingQualifyingDateFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.StructureBuildingQualifyingDatePage
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.StructureBuildingQualifyingDateView

import scala.concurrent.Future

class StructureBuildingQualifyingDateControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new StructureBuildingQualifyingDateFormProvider()
  private def form = formProvider()

  val taxYear = 2024
  val index = 0
  def onwardRoute: Call = Call("GET", "/foo")
  private val isAgentMessageKey = "individual"
  val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)

  lazy val structureBuildingQualifyingDateRoute: String = routes.StructureBuildingQualifyingDateController.onPageLoad(taxYear, NormalMode, index).url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, structureBuildingQualifyingDateRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, structureBuildingQualifyingDateRoute)
      .withFormUrlEncodedBody(
        "structureBuildingQualifyingDate.day"   -> validAnswer.getDayOfMonth.toString,
        "structureBuildingQualifyingDate.month" -> validAnswer.getMonthValue.toString,
        "structureBuildingQualifyingDate.year"  -> validAnswer.getYear.toString
      )

  "StructureBuildingQualifyingDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[StructureBuildingQualifyingDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, isAgentMessageKey, NormalMode)(getRequest, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(StructureBuildingQualifyingDatePage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val view = application.injector.instanceOf[StructureBuildingQualifyingDateView]

        val result = route(application, getRequest).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, isAgentMessageKey, NormalMode)(getRequest, messages(application)).toString
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
        FakeRequest(POST, structureBuildingQualifyingDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[StructureBuildingQualifyingDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageKey, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
