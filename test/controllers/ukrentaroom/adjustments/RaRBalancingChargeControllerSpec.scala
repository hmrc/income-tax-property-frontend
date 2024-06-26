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

package controllers.ukrentaroom.adjustments

import base.SpecBase
import forms.ukrentaroom.adjustments.RaRBalancingChargeFormProvider
import models.BalancingCharge.format
import models.{BalancingCharge, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.adjustments.RaRBalancingChargePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.adjustments.RaRBalancingChargeView

import java.time.LocalDate
import scala.concurrent.Future

class RaRBalancingChargeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/raRBalanceCharge")

  val taxYear: Int = LocalDate.now.getYear
  lazy val totalIncomeRoute: String = routes.RaRBalancingChargeController.onPageLoad(taxYear, NormalMode).url

  val formProvider = new RaRBalancingChargeFormProvider()
  val form: Form[BalancingCharge] = formProvider("agent")

  "RaRBalancingCharge Controller" - {

    "must return OK and the correct view for a GET when an individual" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, totalIncomeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RaRBalancingChargeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual")(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, totalIncomeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RaRBalancingChargeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, "agent")(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(RaRBalancingChargePage, BalancingCharge(balancingChargeYesNo = true, Some(7689.23)))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, totalIncomeRoute)

        val view = application.injector.instanceOf[RaRBalancingChargeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(BalancingCharge(balancingChargeYesNo = true, Some(7689.23))),
          taxYear,
          NormalMode,
          "agent"
        )(request, messages(application)).toString
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
          FakeRequest(POST, totalIncomeRoute)
            .withFormUrlEncodedBody("raRbalancingChargeYesNo" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, totalIncomeRoute)
            .withFormUrlEncodedBody(("raRbalancingChargeAmount", "87.858585"))

        val boundForm = form.bind(Map("raRbalancingChargeAmount" -> "87.858585"))

        val view = application.injector.instanceOf[RaRBalancingChargeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "agent")(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
