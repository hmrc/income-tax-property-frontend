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

package controllers.ukrentaroom.allowances

import base.SpecBase
import controllers.routes
import controllers.ukrentaroom.allowances.routes._
import forms.ukrentaroom.allowances.RaRCapitalAllowancesForACarFormProvider
import models.{CapitalAllowancesForACar, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.allowances.RaRCapitalAllowancesForACarPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.allowances.RaRCapitalAllowancesForACarView

import java.time.LocalDate
import scala.concurrent.Future

class RaRCapitalAllowancesForACarControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/")
  private val taxYear = LocalDate.now.getYear

  val formProvider = new RaRCapitalAllowancesForACarFormProvider()
  val form: Form[CapitalAllowancesForACar] = formProvider("individual")

  lazy val RaRcapitalAllowancesForACarRoute = RaRCapitalAllowancesForACarController.onPageLoad(taxYear, NormalMode).url

  "RaRCapitalAllowancesForACar Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request = FakeRequest(GET, RaRcapitalAllowancesForACarRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RaRCapitalAllowancesForACarView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, taxYear, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(
          RaRCapitalAllowancesForACarPage,
          CapitalAllowancesForACar(capitalAllowancesForACarYesNo = true, Some(12.34))
        )
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, RaRcapitalAllowancesForACarRoute)

        val view = application.injector.instanceOf[RaRCapitalAllowancesForACarView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(CapitalAllowancesForACar(capitalAllowancesForACarYesNo = true, Some(12.34))),
          NormalMode,
          taxYear,
          "individual"
        )(request, messages(application)).toString
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
        val request =
          FakeRequest(POST, RaRcapitalAllowancesForACarRoute)
            .withFormUrlEncodedBody(
              "raRCapitalAllowancesForACarYesNo"  -> "true",
              "raRCapitalAllowancesForACarAmount" -> "1234"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false).build()

      running(application) {
        val request =
          FakeRequest(POST, RaRcapitalAllowancesForACarRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RaRCapitalAllowancesForACarView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, RaRcapitalAllowancesForACarRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, RaRcapitalAllowancesForACarRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
