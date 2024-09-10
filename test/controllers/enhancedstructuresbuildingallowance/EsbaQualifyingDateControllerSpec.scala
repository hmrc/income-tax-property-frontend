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

package controllers.enhancedstructuresbuildingallowance

import base.SpecBase
import controllers.routes
import forms.enhancedstructuresbuildingallowance.EsbaQualifyingDateFormProvider
import models.{NormalMode, Rentals, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.enhancedstructuresbuildingallowance.EsbaQualifyingDatePage
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.enhancedstructuresbuildingallowance.EsbaQualifyingDateView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class EsbaQualifyingDateControllerSpec extends SpecBase with MockitoSugar {

  lazy val esbaQualifyingDateRoute = controllers.enhancedstructuresbuildingallowance.routes.EsbaQualifyingDateController
    .onPageLoad(taxYear, index, NormalMode, Rentals)
    .url
  override val emptyUserAnswers = UserAnswers(userAnswersId)
  val formProvider = new EsbaQualifyingDateFormProvider()
  val validAnswer = LocalDate.now(ZoneOffset.UTC)
  val taxYear = 2024
  val index = 0

  def onwardRoute = Call("GET", "/foo")

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, esbaQualifyingDateRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, esbaQualifyingDateRoute)
      .withFormUrlEncodedBody(
        "esbaQualifyingDate.day"   -> validAnswer.getDayOfMonth.toString,
        "esbaQualifyingDate.month" -> validAnswer.getMonthValue.toString,
        "esbaQualifyingDate.year"  -> validAnswer.getYear.toString
      )

  private def form = formProvider()

  "EsbaQualifyingDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[EsbaQualifyingDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, index, NormalMode, Rentals)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(EsbaQualifyingDatePage(index, Rentals), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), false).build()

      running(application) {
        val view = application.injector.instanceOf[EsbaQualifyingDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, index, NormalMode, Rentals)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), false)
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

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      val request =
        FakeRequest(POST, esbaQualifyingDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EsbaQualifyingDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, index, NormalMode, Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
