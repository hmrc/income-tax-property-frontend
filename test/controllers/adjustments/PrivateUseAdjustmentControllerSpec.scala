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

package controllers.adjustments

import base.SpecBase
import forms.adjustments.PrivateUseAdjustmentFormProvider
import models.{NormalMode, PrivateUseAdjustment, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustments.PrivateUseAdjustmentPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.adjustments.PrivateUseAdjustmentView

import java.time.LocalDate
import scala.concurrent.Future

class PrivateUseAdjustmentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/private-use-adjustment")

  val taxYear: Int = LocalDate.now.getYear
  lazy val privateUseAdjustmentRoute: String = routes.PrivateUseAdjustmentController.onPageLoad(taxYear, NormalMode).url

  val formProvider = new PrivateUseAdjustmentFormProvider()
  val form: Form[PrivateUseAdjustment] = formProvider("individual")

  "PrivateUseAdjustmentController Controller" - {

    "must return OK and the correct view for a GET when an individual" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, privateUseAdjustmentRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PrivateUseAdjustmentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, privateUseAdjustmentRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PrivateUseAdjustmentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "agent")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(PrivateUseAdjustmentPage, PrivateUseAdjustment(7689.23)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, privateUseAdjustmentRoute)

        val view = application.injector.instanceOf[PrivateUseAdjustmentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(PrivateUseAdjustment(7689.23)), taxYear, NormalMode, "agent")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted for yes" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, privateUseAdjustmentRoute)
            .withFormUrlEncodedBody("privateUseAdjustmentAmount" -> "648.98")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, privateUseAdjustmentRoute)
            .withFormUrlEncodedBody(("privateUseAdjustmentAmount", "87.858585"))

        val boundForm = form.bind(Map("privateUseAdjustmentAmount" -> "87.858585"))

        val view = application.injector.instanceOf[PrivateUseAdjustmentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "agent")(request, messages(application)).toString
      }
    }
  }
}
