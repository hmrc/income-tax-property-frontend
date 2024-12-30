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

package controllers.foreign.structurebuildingallowance

import base.SpecBase
import controllers.foreign.structuresbuildingallowance.routes.ForeignStructureBuildingQualifyingAmountController
import forms.foreign.structurebuildingallowance.ForeignStructureBuildingQualifyingAmountFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.structurebuildingallowance.ForeignStructureBuildingQualifyingAmountPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.structurebuildingallowance.ForeignStructureBuildingQualifyingAmountView

import scala.concurrent.Future

class ForeignStructureBuildingQualifyingAmountControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new ForeignStructureBuildingQualifyingAmountFormProvider()
  private val isAgentMessageKey = "individual"
  val form: Form[BigDecimal] = formProvider()

  def onwardRoute: Call = Call("GET", "/foo")

  val validAnswer: BigDecimal = BigDecimal(0)
  val taxYear = 2023
  val index = 0
  val countryCode = "AUS"

  lazy val requestRoute: String =
    ForeignStructureBuildingQualifyingAmountController.onPageLoad(taxYear, countryCode, index, NormalMode).url

  "ForeignStructureBuildingQualifyingAmount Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, requestRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignStructureBuildingQualifyingAmountView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, countryCode, index, isAgentMessageKey, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(ForeignStructureBuildingQualifyingAmountPage(countryCode, index), validAnswer)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, requestRoute)

        val view = application.injector.instanceOf[ForeignStructureBuildingQualifyingAmountView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(validAnswer),
          taxYear,
          countryCode,
          index,
          isAgentMessageKey,
          NormalMode
        )(
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
          FakeRequest(POST, requestRoute)
            .withFormUrlEncodedBody(("foreignStructureBuildingQualifyingAmount", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, requestRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ForeignStructureBuildingQualifyingAmountView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, countryCode, index, isAgentMessageKey, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, requestRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, requestRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
