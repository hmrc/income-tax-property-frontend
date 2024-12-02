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

package controllers.foreign.income

import base.SpecBase
import forms.foreign.income.ForeignReversePremiumsReceivedFormProvider
import models.{NormalMode, ReversePremiumsReceived, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.income.ForeignReversePremiumsReceivedPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.ForeignReversePremiumsReceivedView

import java.time.LocalDate
import scala.concurrent.Future

class ForeignReversePremiumsReceivedControllerSpec extends SpecBase with MockitoSugar {

  private val taxYear = LocalDate.now.getYear
  val countryCode = "BRA"
  def onwardRoute: Call = Call("GET", foreignOtherIncomeFromPropertyRoute)
  val formProvider = new ForeignReversePremiumsReceivedFormProvider()
  val form: Form[ReversePremiumsReceived] = formProvider("individual")

  lazy val foreignReversePremiumsReceivedRoute: String =
    controllers.foreign.income.routes.ForeignReversePremiumsReceivedController
      .onPageLoad(taxYear, countryCode, NormalMode)
      .url
  lazy val foreignOtherIncomeFromPropertyRoute: String =
    controllers.foreign.income.routes.ForeignOtherIncomeFromPropertyController
      .onPageLoad(taxYear, countryCode, NormalMode)
      .url

  "ForeignReversePremiumsReceived Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val view = application.injector.instanceOf[ForeignReversePremiumsReceivedView]

        val foreignRequest = FakeRequest(GET, foreignReversePremiumsReceivedRoute)
        val foreignResult = route(application, foreignRequest).value

        status(foreignResult) mustEqual OK
        contentAsString(foreignResult) mustEqual view(form, NormalMode, taxYear, "individual", countryCode)(
          foreignRequest,
          messages(application)
        ).toString

      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val foreignUserAnswers = UserAnswers(userAnswersId)
        .set(
          ForeignReversePremiumsReceivedPage(countryCode),
          ReversePremiumsReceived(reversePremiumsReceived = true, Some(12.34))
        )
        .success
        .value

      val foreignApplication = applicationBuilder(userAnswers = Some(foreignUserAnswers), isAgent = false).build()

      running(foreignApplication) {
        val request = FakeRequest(GET, foreignReversePremiumsReceivedRoute)

        val view = foreignApplication.injector.instanceOf[ForeignReversePremiumsReceivedView]

        val result = route(foreignApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ReversePremiumsReceived(reversePremiumsReceived = true, Some(12.34))),
          NormalMode,
          taxYear,
          "individual",
          countryCode
        )(request, messages(foreignApplication)).toString
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
        val foreignRequest =
          FakeRequest(POST, foreignReversePremiumsReceivedRoute)
            .withFormUrlEncodedBody("reversePremiumsReceived" -> "true", "reversePremiumsReceivedAmount" -> "1234")

        val foreignResult = route(application, foreignRequest).value

        status(foreignResult) mustEqual SEE_OTHER
        redirectLocation(foreignResult).value mustEqual onwardRoute.url

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val foreignRequest =
          FakeRequest(POST, foreignReversePremiumsReceivedRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ForeignReversePremiumsReceivedView]

        val foreignResult = route(application, foreignRequest).value

        status(foreignResult) mustEqual BAD_REQUEST
        contentAsString(foreignResult) mustEqual view(boundForm, NormalMode, taxYear, "individual", countryCode)(
          foreignRequest,
          messages(application)
        ).toString

      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val foreignRequest = FakeRequest(GET, foreignReversePremiumsReceivedRoute)

        val foreignResult = route(application, foreignRequest).value

        status(foreignResult) mustEqual SEE_OTHER
        redirectLocation(foreignResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val foreignRequest =
          FakeRequest(POST, foreignReversePremiumsReceivedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val foreignResult = route(application, foreignRequest).value

        status(foreignResult) mustEqual SEE_OTHER
        redirectLocation(foreignResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }
  }
}
