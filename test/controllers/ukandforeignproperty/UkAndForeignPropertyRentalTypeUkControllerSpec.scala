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

package controllers.ukandforeignproperty

import base.SpecBase
import controllers.routes
import controllers.ukandforeignproperty.routes.UkAndForeignPropertyRentalTypeUkController
import forms.UkAndForeignPropertyRentalTypeUkFormProvider
import models.{NormalMode, UkAndForeignPropertyRentalTypeUk, UserAnswers}
import navigation.{FakeUKAndForeignPropertyNavigator, UkAndForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.UkAndForeignPropertyRentalTypeUkPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import testHelpers.FakeAuthConnector
import testHelpers.Retrievals.Ops
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel}
import views.html.UkAndForeignPropertyRentalTypeUkView

import scala.concurrent.Future

class UkAndForeignPropertyRentalTypeUkControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")
  val taxYear = 2024

  lazy val ukAndForeignPropertyRentalTypeUkRoute: String = UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, NormalMode).url

  val formProvider = new UkAndForeignPropertyRentalTypeUkFormProvider()
  val form: Form[Set[UkAndForeignPropertyRentalTypeUk]] = formProvider("individual")

  "UkAndForeignPropertyRentalTypeUk Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, ukAndForeignPropertyRentalTypeUkRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UkAndForeignPropertyRentalTypeUkView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val authConnector = new FakeAuthConnector(Some(Agent) ~ ConfidenceLevel.L200)

      val userAnswers = UserAnswers(userAnswersId).set(UkAndForeignPropertyRentalTypeUkPage, UkAndForeignPropertyRentalTypeUk.values.toSet).success.value

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        isAgent = false)
        .overrides(bind[AuthConnector].toInstance(authConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, ukAndForeignPropertyRentalTypeUkRoute)

        val view = application.injector.instanceOf[UkAndForeignPropertyRentalTypeUkView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(UkAndForeignPropertyRentalTypeUk.values.toSet), taxYear, NormalMode, "individual")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val authConnector = new FakeAuthConnector(Some(Agent) ~ ConfidenceLevel.L200)
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[UkAndForeignPropertyNavigator].toInstance(new FakeUKAndForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .overrides(bind[AuthConnector].toInstance(authConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, ukAndForeignPropertyRentalTypeUkRoute)
            .withFormUrlEncodedBody(("value[0]", UkAndForeignPropertyRentalTypeUk.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val authConnector = new FakeAuthConnector(Some(Agent) ~ ConfidenceLevel.L200)
      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        isAgent = false)
        .overrides(bind[AuthConnector].toInstance(authConnector))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, ukAndForeignPropertyRentalTypeUkRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UkAndForeignPropertyRentalTypeUkView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "individual")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, ukAndForeignPropertyRentalTypeUkRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, ukAndForeignPropertyRentalTypeUkRoute)
            .withFormUrlEncodedBody(("value[0]", UkAndForeignPropertyRentalTypeUk.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
