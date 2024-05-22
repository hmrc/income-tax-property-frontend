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

package controllers.furnishedholidaylettings

import base.SpecBase
import controllers.routes
import forms.furnishedholidaylettings.FhlMoreThanOneFormProvider
import models.requests.DataRequest
import models.{NormalMode, User, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.furnishedholidaylettings.FhlMoreThanOnePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.furnishedholidaylettings.FhlMoreThanOneView

import scala.concurrent.Future

class FhlMoreThanOneControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new FhlMoreThanOneFormProvider()
  val taxYear = 2024

  lazy val fhlMoreThanOneRoute = controllers.furnishedholidaylettings.routes.FhlMoreThanOneController.onPageLoad(taxYear, NormalMode).url
  val scenarios = Table[Boolean, String](
    ("isAgency", "AgencyOrIndividual"),
    (true, "agent"),
    (false, "individual"))

  forAll(scenarios) { (isAgency: Boolean, agencyOrIndividual: String) => {
    val form = formProvider(agencyOrIndividual)
    val user = User(
      "",
      "",
      "",
      isAgency,
      Some("agentReferenceNumber")
    )
    s"FhlMoreThanOne Controller for $agencyOrIndividual" - {

      s"must return OK and the correct view for a GET for $agencyOrIndividual" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgency).build()

        running(application) {
          val fakeRequest = FakeRequest(GET, fhlMoreThanOneRoute)
          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

          val result = route(application, request).value

          val view = application.injector.instanceOf[FhlMoreThanOneView]

          status(result) mustEqual OK
          val contentText = contentAsString(result)
          contentText mustEqual view(form, taxYear, NormalMode)(request, messages(application)).toString
          val doesContainAgentRelatedWord = contentText.contains("client")
          if (isAgency) {
            doesContainAgentRelatedWord mustBe true
          } else {
            doesContainAgentRelatedWord mustBe false
          }
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(FhlMoreThanOnePage, isAgency).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgency).build()

        running(application) {
          val fakeRequest = FakeRequest(GET, fhlMoreThanOneRoute)
          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

          val view = application.injector.instanceOf[FhlMoreThanOneView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(form.fill(isAgency), taxYear, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgency)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val fakeRequest =
            FakeRequest(POST, fhlMoreThanOneRoute)
              .withFormUrlEncodedBody(("fhlMoreThanOneFormProvider", "true"))
          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgency).build()

        running(application) {
          val fakeRequest =
            FakeRequest(POST, fhlMoreThanOneRoute)
              .withFormUrlEncodedBody(("fhlMoreThanOneFormProvider", ""))
          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)
          val boundForm = form.bind(Map("fhlMoreThanOneFormProvider" -> ""))

          val view = application.injector.instanceOf[FhlMoreThanOneView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgency).build()

        running(application) {
          val fakeRequest = FakeRequest(GET, fhlMoreThanOneRoute)
          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgency).build()

        running(application) {
          val fakeRequest =
            FakeRequest(POST, fhlMoreThanOneRoute)
              .withFormUrlEncodedBody(("fhlMoreThanOneFormProvider", "true"))
          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
    }
  }
}
