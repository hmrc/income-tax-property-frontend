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

package controllers.ukrentaroom.expenses

import base.SpecBase
import controllers.routes
import forms.ukrentaroom.expenses.LegalManagementOtherFeeFormProvider
import models.requests.DataRequest
import models.{NormalMode, User, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.ukrentaroom.expenses.LegalManagementOtherFeeRRPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.expenses.LegalManagementOtherFeeRRView

import scala.concurrent.Future

class LegalManagementOtherFeeRRControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new LegalManagementOtherFeeFormProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = BigDecimal(14.2)

  lazy val legalManagementOtherFeeRoute =
    controllers.ukrentaroom.expenses.routes.LegalManagementOtherFeeRRController.onPageLoad(taxYear, NormalMode).url

  val taxYear = 2024
  val scenarios = Table[Boolean, String](("Is Agent", "AgencyOrIndividual"), (true, "agent"), (false, "individual"))

  forAll(scenarios) { (isAgent: Boolean, agencyOrIndividual: String) =>
    val form = formProvider(agencyOrIndividual)
    val user = User(
      "",
      "",
      "",
      Option.when(isAgent)("agentReferenceNumber")
    )
    s"LegalManagementOtherFee Controller for $agencyOrIndividual" - {

      "must return OK and the correct view for a GET" in {
        val userAnswers = emptyUserAnswers
        val application = applicationBuilder(Some(userAnswers), isAgent).build()

        running(application) {
          val fakeRequest = FakeRequest(GET, legalManagementOtherFeeRoute)
          val request = DataRequest(fakeRequest, "", user, userAnswers)
          val result = route(application, request).value

          val view = application.injector.instanceOf[LegalManagementOtherFeeRRView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(LegalManagementOtherFeeRRPage, validAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent).build()

        running(application) {
          val fakeRequest = FakeRequest(GET, legalManagementOtherFeeRoute)
          val request = DataRequest(fakeRequest, "", user, userAnswers)
          val view = application.injector.instanceOf[LegalManagementOtherFeeRRView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, NormalMode)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, legalManagementOtherFeeRoute)
              .withFormUrlEncodedBody(("legalManagementOtherFee", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val userAnswers = emptyUserAnswers
        val application = applicationBuilder(Some(userAnswers), isAgent).build()

        running(application) {
          val fakeRequest = FakeRequest(POST, legalManagementOtherFeeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))
          val request = DataRequest(fakeRequest, "", user, userAnswers)

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[LegalManagementOtherFeeRRView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val request = FakeRequest(GET, legalManagementOtherFeeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val request =
            FakeRequest(POST, legalManagementOtherFeeRoute)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
